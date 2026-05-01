/**
 * onharu-backend-v2 예약 관련 HTTP 헬퍼 (세션 쿠키)
 * - 로그인으로 `JSESSIONID`를 얻고, 이후 요청에 `Cookie`로 붙여 호출합니다.
 *
 * 목차 (총 9개)
 * - 🔐 인증 / 사용자: `login`, `getMe`
 * - 🏪 사장(OWNER): `ownerCreateSchedules`, `ownerDeleteSchedules`
 * - 👶 사용자(CHILD): `childReserveSync`, `childReserveAsync`, `childListReservations`, `childCancelReservation`
 * - 🔄 기타(비동기 결과 확인): `pollOnce`
 */
import http from 'k6/http';

const jsonHeaders = { 'Content-Type': 'application/json', Accept: 'application/json' };

/**
 * 쿠키(`JSESSIONID=...`)와 extra 헤더를 합쳐 요청 헤더를 만듭니다.
 */
function mergeHeaders(cookie, extra) {
	const h = Object.assign({ Accept: 'application/json' }, extra || {});
	if (cookie) {
		h.Cookie = cookie;
	}
	return h;
}

/**
 * POST `/api/users/login` → 성공 시 `{ ok:true, cookie:'JSESSIONID=...' }` 반환.
 */
export function login(baseUrl, loginId, password) {
	const jar = http.cookieJar();
	jar.clear(baseUrl, 'JSESSIONID');

	const res = http.post(
		`${baseUrl}/api/users/login`,
		JSON.stringify({ loginId, password }),
		{
			headers: jsonHeaders,
			tags: { name: 'POST /api/users/login' },
			timeout: '30s',
		}
	);
	if (res.status !== 200) {
		return { ok: false, status: res.status, body: String(res.body).slice(0, 300), cookie: '' };
	}
	const cookie = cookieHeaderFromResponse(res, baseUrl);
	return { ok: true, status: res.status, cookie };
}

/**
 * 로그인 응답에서 `JSESSIONID`를 추출해 `JSESSIONID=...` 문자열로 반환(없으면 '').
 */
function cookieHeaderFromResponse(res, baseUrl) {
	const jar = res.cookies;
	if (jar && jar.JSESSIONID && jar.JSESSIONID.length > 0 && jar.JSESSIONID[0].value) {
		return `JSESSIONID=${jar.JSESSIONID[0].value}`;
	}

	const setCookie = res.headers['Set-Cookie'] || res.headers['set-cookie'];
	const match = setCookie && String(setCookie).match(/(?:^|,\s*)JSESSIONID=([^;,\s]+)/);
	if (match && match[1]) {
		return `JSESSIONID=${match[1]}`;
	}

	const cookieJar = http.cookieJar();
	const stored = cookieJar.cookiesForURL(baseUrl);
	if (!stored || !stored.JSESSIONID) return '';
	const storedValue = Array.isArray(stored.JSESSIONID) ? stored.JSESSIONID[0] : stored.JSESSIONID;
	return storedValue ? `JSESSIONID=${storedValue}` : '';
}

/**
 * GET `/api/users/me` — 쿠키가 유효한지 확인용.
 */
export function getMe(baseUrl, cookie) {
	return http.get(`${baseUrl}/api/users/me`, {
		headers: mergeHeaders(cookie),
		tags: { name: 'GET /api/users/me' },
		timeout: '30s',
	});
}

/**
 * 스케줄 생성
 * POST /api/owners/stores/{storeId}/schedules
 * storeSchedules: [{ scheduleDate, startTime, endTime, maxPeople }]
 */
export function ownerCreateSchedules(baseUrl, ownerCookie, storeId, storeSchedules) {
	const res = http.post(
		`${baseUrl}/api/owners/stores/${storeId}/schedules`,
		JSON.stringify({ storeSchedules }),
		{
			headers: mergeHeaders(ownerCookie, jsonHeaders),
			tags: { name: 'POST /api/owners/.../schedules' },
			timeout: '60s',
		}
	);
	return res;
}

/**
 * 공개 슬롯 조회
 * GET `/api/stores/{storeId}/schedules?year&month&day`
 * year: 2026, month: 5, day: 1
 */
export function getPublicScheduleSlots(baseUrl, storeId, year, month, day) {
	const qs = `year=${year}&month=${month}&day=${day}`;
	const res = http.get(`${baseUrl}/api/stores/${storeId}/schedules?${qs}`, {
		headers: { Accept: 'application/json' },
		tags: { name: 'GET /api/stores/.../schedules' },
		timeout: '30s',
	});
	return res;
}

/**
 * 동기 예약 생성
 * POST `/api/childrens/stores/{storeId}/reservations`
 * storeScheduleId: 스케줄 ID
 * people: 인원
 */
export function childReserveSync(baseUrl, childCookie, storeId, storeScheduleId, people) {
	const res = http.post(
		`${baseUrl}/api/childrens/stores/${storeId}/reservations`,
		JSON.stringify({
			storeScheduleId,
			people,
		}),
		{
			headers: mergeHeaders(childCookie, jsonHeaders),
			tags: { name: 'POST sync reservation', mode: 'http' },
			timeout: '60s',
		}
	);
	return res;
}

/**
 * POST `asyncUrl` — 비동기 예약 요청(백엔드별 차이는 여기서 흡수).
 */
export function childReserveAsync(asyncUrl, childCookie, storeId, storeScheduleId, people, correlationId) {
	const payload = {
		storeId,
		storeScheduleId,
		people,
		correlationId,
	};
	const res = http.post(asyncUrl, JSON.stringify(payload), {
		headers: mergeHeaders(childCookie, jsonHeaders),
		tags: { name: 'POST async reservation', mode: 'async' },
		timeout: '60s',
	});
	return res;
}

/**
 * GET `/api/childrens/reservations` — 예약 목록 조회(pageNum 1-based).
 */
export function childListReservations(baseUrl, childCookie, pageNum, perPage) {
	const qs = `pageNum=${pageNum}&perPage=${perPage}&statusFilter=ALL&sortField=id&sortDirection=desc`;
	const res = http.get(`${baseUrl}/api/childrens/reservations?${qs}`, {
		headers: mergeHeaders(childCookie),
		tags: { name: 'GET /api/childrens/reservations' },
		timeout: '60s',
	});
	return res;
}

/**
 * POST `/api/childrens/reservations/{id}/cancel` — 예약 취소(이유 없으면 기본값).
 */
export function childCancelReservation(baseUrl, childCookie, reservationId, cancelReason) {
	const res = http.post(
		`${baseUrl}/api/childrens/reservations/${reservationId}/cancel`,
		JSON.stringify({ cancelReason: cancelReason || 'k6 teardown' }),
		{
			headers: mergeHeaders(childCookie, jsonHeaders),
			tags: { name: 'POST cancel reservation' },
			timeout: '30s',
		}
	);
	return res;
}

/**
 * DELETE `/api/owners/stores/{storeId}/schedules` — 스케줄 일괄 삭제(테스트 정리용).
 */
export function ownerDeleteSchedules(baseUrl, ownerCookie, storeId, storeScheduleIds) {
	const res = http.del(
		`${baseUrl}/api/owners/stores/${storeId}/schedules`,
		JSON.stringify({ storeScheduleIds }),
		{
			headers: mergeHeaders(ownerCookie, jsonHeaders),
			tags: { name: 'DELETE /api/owners/.../schedules' },
			timeout: '60s',
		}
	);
	return res;
}

/**
 * (선택) 폴링 1회 — `urlTemplate`의 `{id}`를 치환해 GET 호출.
 */
export function pollOnce(urlTemplate, childCookie, id) {
	const url = urlTemplate.split('{id}').join(String(id));
	const res = http.get(url, {
		headers: mergeHeaders(childCookie),
		tags: { name: 'async poll' },
		timeout: '10s',
	});
	return res;
}
