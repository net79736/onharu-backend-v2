import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';
import { check, fail } from 'k6';

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
 * api.js 함수들이 “최소 1회 정상 동작하는지”만 확인하는 스모크.
 * - 의도: 성능 측정이 아니라, 엔드포인트/인증/바디가 맞는지 빠르게 검증
 * - 성공 기준: 각 API가 기대 status(200/201/202...)를 반환
 * 
 * 실행 순서
 * setup() → default() → teardown()
 * 
 * 실행 방법
 * ```bash
 * K6_OWNER_LOGIN_ID='owner123@test.com' \
 * K6_OWNER_PASSWORD='password123!' \
 * K6_CHILD_LOGIN_ID='child123@test.com' \ 
 * K6_CHILD_PASSWORD='Password123!' \
 * K6_STORE_ID='1' \
 * k6 run reservation-compare/api-smoke.js
 * ```
 */
const cfg = getConfig();
let createdReservationId = null;

export const options = {
	scenarios: {
		smoke: {
			executor: 'per-vu-iterations',
			vus: 1,
			iterations: 1,
			maxDuration: '2m',
		},
	},
	thresholds: {
		// 이 스크립트는 “모든 체크 통과”를 성공 기준으로 삼습니다.
		// (환경/데이터 상태에 따라 409 같은 응답이 나올 수 있어 http_req_failed로 실패 처리하면 너무 취약해짐)
		checks: ['rate==1.0'],
	},
};

/**
 * JSON 파싱 실패 시 에러 발생
 * 
 * @param {*} res 
 * @param {*} label 
 * @returns 
 */
function mustJson(res, label) {
	try {
		return JSON.parse(res.body);
	} catch (e) {
		fail(`${label} JSON 파싱 실패: ${e} body=${String(res.body).slice(0, 300)}`);
		return null;
	}
}

/**
 * 쿠키(JSESSIONID) 확보 실패 시 에러 발생
 * 
 * @param {*} cookie 
 * @param {*} label 
 */
function mustCookie(cookie, label) {
	if (!cookie || !String(cookie).includes('JSESSIONID=')) {
		fail(`${label} 쿠키(JSESSIONID) 확보 실패`);
	}
}

/**
 * 08~19시 범위에서 선택한 시간을 반환
 * 
 * @param {*} seed
 * @returns 
 */
function pickHour(seed) {
	// 08~19시 범위에서 선택(운영 규칙에 맞춰 필요 시 조정)
	const base = 8;
	const span = 12; // 8..19 (12개)
	return base + (seed % span);
}

/**
 * “내일” 09:00~10:00 슬롯 1개만 생성 (스모크용)
 * 
 * @param {*} seed 
 * @returns 
 */
function buildSingleScheduleRequest(seed) {
	// “내일” 09:00~10:00 슬롯 1개만 생성 (스모크용)
	const d = new Date();
	d.setDate(d.getDate() + 1);
	const y = d.getFullYear();
	const m = String(d.getMonth() + 1).padStart(2, '0');
	const day = String(d.getDate()).padStart(2, '0');
	const hour = pickHour(seed);
	const hh = String(hour).padStart(2, '0');
	const hh2 = String(hour + 1).padStart(2, '0');
	return {
		scheduleDate: `${y}-${m}-${day}`,
		startTime: `${hh}:00`,
		endTime: `${hh2}:00`,
		maxPeople: 10,
	};
}

/**
 * 공개 슬롯 조회 결과에서 스케줄 ID 추출
 * 
 * @param {*} getRes 
 * @param {*} startTime 
 * @returns 
 */
function extractScheduleSlotId(getRes, startTime) {
	const j = mustJson(getRes, '공개 슬롯 조회');
	const slots = j?.data?.scheduleSlots || [];
	if (!slots.length) fail('공개 슬롯이 비어있습니다(스케줄 생성/조회 확인 필요)');
	const stKey = String(startTime || '').slice(0, 5);
	const preferred = slots.find((s) => String(s?.startTime || '').slice(0, 5) === stKey) || slots[0];
	const id = preferred?.id;
	if (!id) fail('공개 슬롯에서 id를 찾지 못했습니다.');
	return id;
}

/**
 * 예약 응답에서 예약 ID 추출
 * 
 * @param {*} res 
 * @returns 
 */
function extractReservationIdFromResponse(res) {
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

/**
 * 예약 목록에서 취소 가능한 예약 ID 추출
 * 
 * @param {*} listRes 
 * @param {*} storeId 
 * @returns 
 */
function findCancelableReservationId(listRes, storeId) {
	try {
		const j = JSON.parse(listRes.body);
		const rows = j?.data?.reservations || [];
		for (const row of rows) {
			if (!row || typeof row.id !== 'number') continue;
			if (typeof row.storeId === 'number' && row.storeId !== storeId) continue;
			const st = row.status;
			if (st === 'CANCELED' || st === 'COMPLETED') continue;
			return row.id;
		}
	} catch (_) {}
	return null;
}

/**
 * 설정 검증 및 로그인
 * 
 * @returns 
 */
export function setup() {
	const miss = validateConfig(cfg);
	if (miss.length) fail(`[k6] 환경 변수 부족: ${miss.join(', ')}`);

	// 1) 로그인 (OWNER/CHILD)
	const ownerLogin = login(cfg.baseUrl, cfg.ownerLoginId, cfg.ownerPassword);
	check(ownerLogin, { 'OWNER 로그인 200': (r) => r.ok && r.status === 200 });
	mustCookie(ownerLogin.cookie, 'OWNER 로그인');

	const childLogin = login(cfg.baseUrl, cfg.childLoginId, cfg.childPassword);
	check(childLogin, { 'CHILD 로그인 200': (r) => r.ok && r.status === 200 });
	mustCookie(childLogin.cookie, 'CHILD 로그인');

	// 2) /me 확인
	const ownerMe = getMe(cfg.baseUrl, ownerLogin.cookie);
	check(ownerMe, { 'OWNER /me 200': (r) => r.status === 200 });

	const childMe = getMe(cfg.baseUrl, childLogin.cookie);
	check(childMe, { 'CHILD /me 200': (r) => r.status === 200 });

	// 3) 스케줄 1개 생성 (충돌 가능성이 있어서 시간대를 바꿔가며 몇 번 재시도)
	let req = null;
	let createRes = null;
	for (let i = 0; i < 5; i++) {
		req = buildSingleScheduleRequest((Date.now() + i * 997) >>> 0);
		createRes = ownerCreateSchedules(cfg.baseUrl, ownerLogin.cookie, cfg.storeId, [req]);
		if (createRes.status === 201) break;
	}
	check(createRes, { '스케줄 생성 201': (r) => r && r.status === 201 });

	// 4) 공개 슬롯 조회 → scheduleSlotId 확보
	const parts = req.scheduleDate.split('-').map((x) => Number(x));
	const getRes = getPublicScheduleSlots(cfg.baseUrl, cfg.storeId, parts[0], parts[1], parts[2]);
	check(getRes, { '공개 슬롯 조회 200': (r) => r.status === 200 });
	const storeScheduleId = extractScheduleSlotId(getRes, req.startTime);
	console.log('🐧🐧🐧 storeScheduleId = ', storeScheduleId);

	return {
		ownerCookie: ownerLogin.cookie,
		childCookie: childLogin.cookie,
		storeScheduleId,
		scheduleDate: req.scheduleDate,
	};
}

export default function (data) {
	// 5) 동기 예약
	const reserveRes = childReserveSync(
		cfg.baseUrl,
		data.childCookie,
		cfg.storeId,
		data.storeScheduleId,
		1
	);
	check(reserveRes, { '동기 예약 201': (r) => r.status === 201 });

	// 6) 목록 조회(최소 동작 확인)
	const listRes = childListReservations(cfg.baseUrl, data.childCookie, 1, 10);
	console.log('🐧🐧🐧 listRes = ', listRes);
	check(listRes, { '예약 목록 200': (r) => r.status === 200 });

	// 7) 예약 취소(가능하면 방금 만든 예약을 취소)
	let reservationId = extractReservationIdFromResponse(reserveRes);
	if (reservationId == null) {
		// 응답 바디에 id가 없으면 목록에서 “취소 가능한 것” 하나를 찾아 정리
		reservationId = findCancelableReservationId(listRes, cfg.storeId);
	}

	if (reservationId != null) {
		const cancelRes = childCancelReservation(cfg.baseUrl, data.childCookie, reservationId, 'k6 smoke');
		check(cancelRes, { '예약 취소 200': (r) => r.status === 200 });
		createdReservationId = reservationId;
	}

	// 8) (옵션) 비동기 예약 + 폴링 1회
	if ((cfg.mode || '').toLowerCase() === 'async') {
		const correlationId = `k6-smoke-${Date.now()}`;
		const asyncRes = childReserveAsync(
			cfg.asyncReserveUrl,
			data.childCookie,
			cfg.storeId,
			data.storeScheduleId,
			1,
			correlationId
		);
		check(asyncRes, { '비동기 예약 수락(2xx)': (r) => r.status >= 200 && r.status < 300 });

		// 폴링 템플릿이 있으면 1회 호출만(스모크 목적)
		if (cfg.asyncPollTemplate) {
			let asyncReservationId = null;
			try {
				const j = JSON.parse(asyncRes.body);
				asyncReservationId =
					(typeof j?.data?.reservationId === 'number' && j.data.reservationId) ||
					(typeof j?.data?.id === 'number' && j.data.id) ||
					(typeof j?.data?.reservation?.id === 'number' && j.data.reservation.id) ||
					null;
			} catch (_) {}
			if (asyncReservationId != null) {
				const pollRes = pollOnce(cfg.asyncPollTemplate, data.childCookie, asyncReservationId);
				check(pollRes, { '비동기 폴링 응답(2xx/404 가능)': (r) => r.status >= 200 && r.status < 500 });
			}
		}
	}
}

export function teardown(data) {
	if (!data?.ownerCookie) return;

	// setup에서 확보한 슬롯 id만 삭제 (불필요한 GET/과다삭제로 4xx/5xx 나는 케이스 방지)
	try {
		if (typeof data.storeScheduleId === 'number') {
			const delRes = ownerDeleteSchedules(
				cfg.baseUrl,
				data.ownerCookie,
				cfg.storeId,
				[data.storeScheduleId]
			);
			// teardown은 “정리”이지만, 4xx/5xx를 조용히 숨기면 다음 실행에 영향을 줌
			check(delRes, { '스케줄 삭제 200': (r) => r.status === 200 });
		}
	} catch (_) {}
}

export function handleSummary(data) {
	return {
		stdout: textSummary(data, { indent: ' ', enableColors: false }),
	};
}