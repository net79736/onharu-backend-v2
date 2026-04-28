import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';
import { check, fail, sleep } from 'k6';
import { Counter, Trend } from 'k6/metrics';

import {
	childCancelReservation,
	childListReservations,
	childReserveAsync,
	childReserveSync,
	getMe,
	getPublicScheduleSlots,
	login,
	ownerCreateSchedules,
	ownerDeleteSchedules,
	pollOnce,
} from './api.js';
import { getConfig, validateConfig } from './config.js';

/**
 * 동기(http) vs 비동기(async) 예약을 “필수 흐름”만으로 비교하는 라이트 버전입니다.
 * - 의도: script.js의 복잡한 옵션/리포트/긴 폴링을 걷어내고, 로드 테스트 흐름을 빠르게 이해/검증
 * - 성공 기준: mode에 맞는 예약 요청이 기대 status로 응답(동기=201, 비동기=202 또는 2xx)
 *
 * 실행 순서
 * setup() → default() → teardown()
 *
 * 실행 방법
 * ```bash
 * cd load-test
 * K6_OWNER_LOGIN_ID='owner123@test.com' \
 * K6_OWNER_PASSWORD='Password123!' \
 * K6_CHILD_LOGIN_ID='child123@test.com' \
 * K6_CHILD_PASSWORD='Password123!' \
 * K6_STORE_ID='1' \
 * K6_RESERVE_MODE='http' \
 * k6 run reservation-compare/script-lite.js
 *
 * # 비동기(async)
 * K6_OWNER_LOGIN_ID='owner123@test.com' \
 * K6_OWNER_PASSWORD='Password123!' \
 * K6_CHILD_LOGIN_ID='child123@test.com' \
 * K6_CHILD_PASSWORD='Password123!' \
 * K6_STORE_ID='1' \
 * K6_RESERVE_MODE='async' \
 * K6_ASYNC_RESERVE_URL='http://localhost:8080/api/...' \
 * K6_ASYNC_POLL_URL_TEMPLATE='http://localhost:8080/api/.../{id}' \
 * k6 run reservation-compare/script-lite.js
 * ```
 *
 * 주의
 * - 스케줄 중복(“중복된 일정이 존재합니다”)을 피하려고, DAY_OFFSET이 없으면 storeId+시간 기반 날짜 오프셋을 자동 적용합니다.
 */

const cfg = getConfig();

const trendReserveSync = new Trend('reserve_sync_duration', true);
const trendReserveAsyncAccept = new Trend('reserve_async_accept_duration', true);
const counterReserveOk = new Counter('reserve_ok_total');
const counterReserveFail = new Counter('reserve_fail_total');

export const options = {
	scenarios: {
		reservation_load: {
			executor: 'per-vu-iterations',
			vus: cfg.vus,
			iterations: cfg.iterations,
			maxDuration: cfg.maxDuration,
		},
	},
	thresholds: {
		http_req_failed: ['rate<0.15'],
		http_req_duration: ['p(95)<15000'],
	},
};

function mustJson(res, label) {
	try {
		return JSON.parse(res.body);
	} catch (e) {
		fail(`${label} JSON 파싱 실패: ${e} body=${String(res.body).slice(0, 300)}`);
		return null;
	}
}

function pad2(n) {
	return String(n).padStart(2, '0');
}

function fmtDate(d) {
	const y = d.getFullYear();
	const m = pad2(d.getMonth() + 1);
	const day = pad2(d.getDate());
	return `${y}-${m}-${day}`;
}

function autoDayOffset(storeId) {
	// 실행마다 날짜를 밀어서 기존 스케줄과의 중복 확률을 낮춤(1~60 분산)
	const n = Date.now();
	const seed = (Number(storeId || 0) * 2654435761 + n) >>> 0;
	return 1 + (seed % 60);
}

function baseDate() {
	// 기본: 내일 00:00
	const d = new Date();
	d.setDate(d.getDate() + 1);
	d.setHours(0, 0, 0, 0);

	// 선택: 월 고정(권장) - K6_SCHEDULE_YEAR / K6_SCHEDULE_MONTH
	if (cfg.scheduleYear && cfg.scheduleMonth) {
		const x = new Date(d.getTime());
		x.setFullYear(cfg.scheduleYear);
		x.setMonth(cfg.scheduleMonth - 1);
		x.setDate(1);
		x.setHours(0, 0, 0, 0);
		return x;
	}

	// 선택: 날짜 오프셋 - K6_SCHEDULE_DAY_OFFSET
	const offset =
		cfg.scheduleDayOffset != null && cfg.scheduleDayOffset !== '' && Number(cfg.scheduleDayOffset) > 0
			? Number(cfg.scheduleDayOffset)
			: autoDayOffset(cfg.storeId);
	d.setDate(d.getDate() + offset);
	return d;
}

function buildScheduleRequests(total, slicesPerDay, startDate) {
	const list = [];
	for (let i = 0; i < total; i++) {
		const dayOffset = Math.floor(i / slicesPerDay);
		const slotInDay = i % slicesPerDay;
		const day = new Date(startDate.getTime());
		day.setDate(day.getDate() + dayOffset);

		const hourStart = 8 + slotInDay; // 08:00부터
		list.push({
			scheduleDate: fmtDate(day),
			startTime: `${pad2(hourStart)}:00`,
			endTime: `${pad2(hourStart + 1)}:00`,
			maxPeople: 10,
		});
	}
	return list;
}

function timeKey(t) {
	if (t == null) return '';
	if (typeof t === 'string') return t.length >= 5 ? t.slice(0, 5) : t;
	return String(t).slice(0, 5);
}

function getSlotIdsForRequests(baseUrl, storeId, requests) {
	const byDate = {};
	for (const r of requests) {
		if (!byDate[r.scheduleDate]) byDate[r.scheduleDate] = [];
		byDate[r.scheduleDate].push(r);
	}

	const ids = [];
	const dates = Object.keys(byDate).sort();
	for (const scheduleDate of dates) {
		const [y, m, d] = scheduleDate.split('-').map((x) => Number(x));
		const res = getPublicScheduleSlots(baseUrl, storeId, y, m, d);
		if (res.status !== 200) {
			fail(`스케줄 조회 실패 status=${res.status} body=${String(res.body).slice(0, 200)}`);
		}
		const j = mustJson(res, '스케줄 조회');
		const slots = j?.data?.scheduleSlots || [];

		const map = {};
		for (const s of slots) {
			const key = `${s.scheduleDate}_${timeKey(s.startTime)}`;
			map[key] = s.id;
		}
		for (const req of byDate[scheduleDate]) {
			const key = `${req.scheduleDate}_${timeKey(req.startTime)}`;
			const id = map[key];
			if (!id) fail(`일정 ID를 찾지 못함 key=${key}`);
			ids.push(id);
		}
	}
	return ids;
}

function extractReservationId(res) {
	try {
		const j = JSON.parse(res.body);
		if (j?.data) {
			if (typeof j.data.reservationId === 'number') return j.data.reservationId;
			if (typeof j.data.id === 'number') return j.data.id;
			if (typeof j.data.reservation?.id === 'number') return j.data.reservation.id;
		}
	} catch (_) {}
	const loc = res.headers?.Location || res.headers?.location;
	if (loc) {
		const m = String(loc).match(/(\d+)\s*$/);
		if (m) return Number(m[1]);
	}
	return null;
}

function assertAuthenticatedAs(baseUrl, cookie, expectedUserType, label) {
	if (!cookie || !String(cookie).includes('JSESSIONID=')) {
		fail(`${label} 쿠키(JSESSIONID) 확보 실패`);
	}
	const meRes = getMe(baseUrl, cookie);
	if (meRes.status !== 200) {
		fail(`${label} /me 실패 status=${meRes.status} body=${String(meRes.body).slice(0, 200)}`);
	}
	const j = mustJson(meRes, `${label} /me`);
	if (expectedUserType && j?.data?.userType !== expectedUserType) {
		fail(`${label} userType 불일치 expected=${expectedUserType} actual=${j?.data?.userType}`);
	}
}

function cancelAllForStore(baseUrl, childCookie, storeId) {
	let page = 1;
	const perPage = 50;
	for (let guard = 0; guard < 20; guard++) {
		const res = childListReservations(baseUrl, childCookie, page, perPage);
		if (res.status !== 200) return;
		let j;
		try {
			j = JSON.parse(res.body);
		} catch {
			return;
		}
		const rows = j?.data?.reservations || [];
		const totalPages = j?.data?.totalPages || 1;
		for (const row of rows) {
			if (row?.storeId !== storeId) continue;
			const st = row?.status;
			if (st === 'CANCELED' || st === 'COMPLETED') continue;
			childCancelReservation(baseUrl, childCookie, row.id, 'k6 teardown');
		}
		if (page >= totalPages || rows.length === 0) return;
		page++;
	}
}

export function setup() {
	const miss = validateConfig(cfg);
	if (miss.length) fail(`[k6] 환경 변수 부족: ${miss.join(', ')}`);

	const totalSlots = cfg.vus * cfg.iterations;
	const requests = buildScheduleRequests(totalSlots, cfg.slicesPerDay, baseDate());

	const ownerLogin = login(cfg.baseUrl, cfg.ownerLoginId, cfg.ownerPassword);
	if (!ownerLogin.ok) fail(`OWNER 로그인 실패 status=${ownerLogin.status}`);
	assertAuthenticatedAs(cfg.baseUrl, ownerLogin.cookie, 'OWNER', 'OWNER');

	const childLogin = login(cfg.baseUrl, cfg.childLoginId, cfg.childPassword);
	if (!childLogin.ok) fail(`CHILD 로그인 실패 status=${childLogin.status}`);
	assertAuthenticatedAs(cfg.baseUrl, childLogin.cookie, 'CHILD', 'CHILD');

	const createRes = ownerCreateSchedules(cfg.baseUrl, ownerLogin.cookie, cfg.storeId, requests);
	if (createRes.status !== 201) {
		fail(`스케줄 생성 실패 status=${createRes.status} body=${String(createRes.body).slice(0, 300)}`);
	}

	const scheduleIds = getSlotIdsForRequests(cfg.baseUrl, cfg.storeId, requests);

	return {
		baseUrl: cfg.baseUrl,
		storeId: cfg.storeId,
		mode: cfg.mode,
		ownerCookie: ownerLogin.cookie,
		childCookie: childLogin.cookie,
		scheduleIds,
		asyncReserveUrl: cfg.asyncReserveUrl,
		asyncPollTemplate: cfg.asyncPollTemplate,
		runId: `k6-${Date.now()}`,
	};
}

export default function (data) {
	const idx = (__VU - 1) * cfg.iterations + __ITER;
	const storeScheduleId = data.scheduleIds[idx];
	const correlationId = `${data.runId}-${__VU}-${__ITER}-${Date.now()}`;

	if (data.mode === 'http') {
		const t0 = Date.now();
		const res = childReserveSync(data.baseUrl, data.childCookie, data.storeId, storeScheduleId, 1);
		trendReserveSync.add(Date.now() - t0);
		const ok = check(res, { '동기 예약 201': (r) => r.status === 201 });
		if (ok) counterReserveOk.add(1);
		else counterReserveFail.add(1);
	} else if (data.mode === 'async') {
		const t0 = Date.now();
		const res = childReserveAsync(
			data.asyncReserveUrl,
			data.childCookie,
			data.storeId,
			storeScheduleId,
			1,
			correlationId
		);
		trendReserveAsyncAccept.add(Date.now() - t0);
		const accepted = check(res, {
			'비동기 예약 수락(2xx/202)': (r) => r.status === 202 || (r.status >= 200 && r.status < 300),
		});

		// (옵션) 폴링은 “1회만” (완료시간 측정은 길어지기 쉬워서 lite에서는 생략)
		if (accepted && data.asyncPollTemplate) {
			const reservationId = extractReservationId(res);
			if (reservationId) {
				pollOnce(data.asyncPollTemplate, data.childCookie, reservationId);
			}
		}

		if (accepted) counterReserveOk.add(1);
		else counterReserveFail.add(1);
	} else {
		fail(`알 수 없는 K6_RESERVE_MODE=${data.mode}`);
	}

	sleep(cfg.sleepSec);
}

export function teardown(data) {
	if (!data?.ownerCookie || !data?.childCookie) return;
	try {
		cancelAllForStore(data.baseUrl, data.childCookie, data.storeId);
	} catch (_) {}
	try {
		ownerDeleteSchedules(data.baseUrl, data.ownerCookie, data.storeId, data.scheduleIds);
	} catch (_) {}
}

export function handleSummary(data) {
	return { stdout: textSummary(data, { indent: ' ', enableColors: false }) };
}

