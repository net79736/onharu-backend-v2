/**
 * k6 환경·실행 옵션 (예약 HTTP vs 비동기 비교)
 *
 * 필수:
 *   K6_OWNER_LOGIN_ID, K6_OWNER_PASSWORD — 스케줄 생성/삭제
 *   K6_CHILD_LOGIN_ID, K6_CHILD_PASSWORD — 예약 생성/취소
 *   K6_STORE_ID — 대상 가게 ID
 *
 * 선택:
 *   BASE_URL (기본 http://localhost:8080)
 *   K6_RESERVE_MODE http | async (기본 http)
 *   K6_ASYNC_RESERVE_URL — 비동기 예약 전체 URL (async 모드). 예: http://localhost:8080/api/experimental/reservations/async
 *   K6_ASYNC_POLL_URL_TEMPLATE — (선택) 처리 완료 폴링 URL, {id} 치환. 미설정 시 폴링 생략.
 *   ONHARU_VUS, ONHARU_ITERATIONS, ONHARU_MAX_DURATION, ONHARU_SLEEP_SEC
 *   K6_SLOTS_SLICES_PER_DAY — 하루당 비중복 슬롯 개수 (기본 12)
 *   K6_SCHEDULE_DAY_OFFSET — 스케줄 시작일 오프셋(내일 + N일, 기본 자동)
 *   K6_SCHEDULE_YEAR / K6_SCHEDULE_MONTH — 스케줄 시작 연/월 고정 (예: 2026, 5). 설정 시 DAY_OFFSET보다 우선.
 *   K6_SCHEDULE_PREDELETE — setup에서 겹치는 기존 스케줄 선삭제(0/1, 기본 0)
 */

export function getConfig() {
	const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
	const mode = (__ENV.K6_RESERVE_MODE || 'http').toLowerCase();
	const vus = Number(__ENV.ONHARU_VUS || 50);
	const iterations = Number(__ENV.ONHARU_ITERATIONS || 20);
	const maxDuration = __ENV.ONHARU_MAX_DURATION || '30m';
	const sleepSec =
		__ENV.ONHARU_SLEEP_SEC !== undefined && __ENV.ONHARU_SLEEP_SEC !== ''
			? Number(__ENV.ONHARU_SLEEP_SEC)
			: 0;

	const ownerLoginId = __ENV.K6_OWNER_LOGIN_ID || 'owner123@test.com';
	const ownerPassword = __ENV.K6_OWNER_PASSWORD || 'Password123!';
	const childLoginId = __ENV.K6_CHILD_LOGIN_ID || 'child123@test.com';
	const childPassword = __ENV.K6_CHILD_PASSWORD || 'Password123!';
	const storeId = Number(__ENV.K6_STORE_ID || '0');

	const asyncReserveUrl = __ENV.K6_ASYNC_RESERVE_URL || '';
	const asyncPollTemplate = __ENV.K6_ASYNC_POLL_URL_TEMPLATE || '';

	const slicesPerDay = Number(__ENV.K6_SLOTS_SLICES_PER_DAY || 12);
	const scheduleDayOffsetRaw = __ENV.K6_SCHEDULE_DAY_OFFSET;
	const scheduleDayOffset =
		scheduleDayOffsetRaw !== undefined && scheduleDayOffsetRaw !== ''
			? Number(scheduleDayOffsetRaw)
			: null; // null이면 script.js에서 자동 산출
	const scheduleYearRaw = __ENV.K6_SCHEDULE_YEAR;
	const scheduleMonthRaw = __ENV.K6_SCHEDULE_MONTH;
	const scheduleYear =
		scheduleYearRaw !== undefined && scheduleYearRaw !== '' ? Number(scheduleYearRaw) : null;
	const scheduleMonth =
		scheduleMonthRaw !== undefined && scheduleMonthRaw !== '' ? Number(scheduleMonthRaw) : null;
	const schedulePredelete = String(__ENV.K6_SCHEDULE_PREDELETE || '0') === '1';

	return {
		baseUrl, // 기본 http://localhost:8080
		mode, // http | async
		vus, // 8
		iterations, // 20
		maxDuration, // 30m
		sleepSec, // 0
		ownerLoginId, // owner123@test.com
		ownerPassword, // Password123!
		childLoginId, // child123@test.com
		childPassword, // Password123!
		storeId, // 1
		asyncReserveUrl, // http://localhost:8080/api/experimental/reservations/async
		asyncPollTemplate, // http://localhost:8080/api/experimental/reservations/async/{id}
		slicesPerDay, // 12
		scheduleDayOffset, // null
		scheduleYear, // null
		scheduleMonth, // null
		schedulePredelete, // false
	};
}

export function validateConfig(c) {
	const miss = [];
	if (!c.ownerLoginId || !c.ownerPassword) miss.push('K6_OWNER_LOGIN_ID / K6_OWNER_PASSWORD');
	if (!c.childLoginId || !c.childPassword) miss.push('K6_CHILD_LOGIN_ID / K6_CHILD_PASSWORD');
	if (!c.storeId || c.storeId <= 0) miss.push('K6_STORE_ID');
	if (c.mode === 'async' && !c.asyncReserveUrl) miss.push('K6_ASYNC_RESERVE_URL (async 모드)');
	return miss;
}
