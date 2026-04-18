import { textSummary } from 'https://jslib.k6.io/k6-summary/0.0.1/index.js';
import { check, sleep } from 'k6';
import http from 'k6/http';
import { Counter } from 'k6/metrics';

import { buildHtmlReport } from './html-report.js';
import { runChatSendStomp, wsUrlFromHttpBase } from './stomp-chat-send.js';

/**
 * 예약 + 채팅 도메인 부하 테스트 (k6)
 *
 * 채팅:
 *   - 기본: STOMP WebSocket → /app/chat/send (실제 메시지 전송)
 *   - ONHARU_CHAT_MODE=list → GET /api/chats (목록만, 가벼운 비교용)
 *
 * 전송 모드에서 채팅방/유저 ID:
 *   - setup 시 GET /api/users/me + GET /api/chats 로 첫 방·userId 자동 조회
 *   - 또는 수동: K6_CHAT_ROOM_ID, K6_SENDER_ID
 *   - 채팅방이 하나도 없으면 setup 실패 (UI에서 방을 먼저 만드세요)
 *
 * 인증: K6_COOKIE 또는 K6_BEARER_TOKEN
 *
 * 예약: K6_RESERVATION_ROLE=child|owner
 */

const successCounter = new Counter('onharu_domains_success');
const failureCounter = new Counter('onharu_domains_failure');

const baseUrl = __ENV.BASE_URL || 'http://localhost:8080';
const vus = Number(__ENV.ONHARU_VUS || 100);
const iterations = Number(__ENV.ONHARU_ITERATIONS || 100);
const maxDuration = __ENV.ONHARU_MAX_DURATION || '45m';
const reservationRole = (__ENV.K6_RESERVATION_ROLE || 'child').toLowerCase();
const chatMode = (__ENV.ONHARU_CHAT_MODE || 'send').toLowerCase();

const sleepSec =
	__ENV.ONHARU_SLEEP_SEC !== undefined && __ENV.ONHARU_SLEEP_SEC !== ''
		? Number(__ENV.ONHARU_SLEEP_SEC)
		: 0.05;

if (!__ENV.K6_COOKIE && !__ENV.K6_BEARER_TOKEN) {
	throw new Error(
		'[k6] 인증이 필요합니다. 예: export K6_COOKIE="JSESSIONID=..." (브라우저에서 복사) 또는 K6_BEARER_TOKEN=...'
	);
}

export const options = {
	scenarios: {
		onharu_domains: {
			executor: 'per-vu-iterations',
			vus,
			iterations,
			maxDuration,
		},
	},
	thresholds: {
		http_req_failed: ['rate<0.1'],
		http_req_duration: ['p(95)<8000'],
	},
};

function authHeaders() {
	const h = { Accept: 'application/json' };
	if (__ENV.K6_BEARER_TOKEN) {
		h.Authorization = `Bearer ${__ENV.K6_BEARER_TOKEN}`;
	}
	if (__ENV.K6_COOKIE) {
		h.Cookie = __ENV.K6_COOKIE;
	}
	return h;
}

function wsAuthHeaders() {
	const h = {};
	if (__ENV.K6_BEARER_TOKEN) {
		h.Authorization = `Bearer ${__ENV.K6_BEARER_TOKEN}`;
	}
	if (__ENV.K6_COOKIE) {
		h.Cookie = __ENV.K6_COOKIE;
	}
	return h;
}

function get(url, name) {
	return http.get(url, {
		headers: authHeaders(),
		tags: { name },
		timeout: '30s',
	});
}

function reservationUrl() {
	if (reservationRole === 'owner') {
		return `${baseUrl}/api/owners/reservations/summary`;
	}
	return `${baseUrl}/api/childrens/reservations?pageNum=1&perPage=10&statusFilter=ALL&sortField=id&sortDirection=desc`;
}

/** setup: 채팅 전송에 필요한 userId, chatRoomId (한 번만 HTTP) */
export function setup() {
	if (chatMode === 'list') {
		return { chatMode: 'list' };
	}

	if (__ENV.K6_CHAT_ROOM_ID && __ENV.K6_SENDER_ID) {
		return {
			chatMode: 'send',
			userId: Number(__ENV.K6_SENDER_ID),
			chatRoomId: Number(__ENV.K6_CHAT_ROOM_ID),
		};
	}

	const meRes = http.get(`${baseUrl}/api/users/me`, {
		headers: authHeaders(),
		timeout: '30s',
	});
	if (meRes.status !== 200) {
		throw new Error(
			`[k6] setup GET /api/users/me 실패 status=${meRes.status} body=${String(meRes.body).slice(0, 200)}`
		);
	}
	let userId;
	try {
		userId = JSON.parse(meRes.body).data.userId;
	} catch (e) {
		throw new Error(`[k6] setup /api/users/me JSON 파싱 실패: ${e}`);
	}

	const chatsRes = http.get(`${baseUrl}/api/chats`, {
		headers: authHeaders(),
		timeout: '30s',
	});
	if (chatsRes.status !== 200) {
		throw new Error(`[k6] setup GET /api/chats 실패 status=${chatsRes.status}`);
	}
	let chatRoomId;
	try {
		const rooms = JSON.parse(chatsRes.body).data.chatRoomResponses || [];
		if (rooms.length === 0) {
			throw new Error(
				'[k6] 참여 중인 채팅방이 없습니다. 웹에서 채팅방을 만든 뒤 다시 실행하거나 K6_CHAT_ROOM_ID 를 지정하세요.'
			);
		}
		chatRoomId = rooms[0].chatRoomId;
	} catch (e) {
		if (String(e.message).indexOf('참여 중인') !== -1) throw e;
		throw new Error(`[k6] setup /api/chats JSON 파싱 실패: ${e}`);
	}

	return { chatMode: 'send', userId, chatRoomId };
}

export default function (data) {
	let ok = true;

	if (data.chatMode === 'list') {
		const chatRes = get(`${baseUrl}/api/chats`, 'GET /api/chats');
		const chatOk = check(chatRes, {
			'GET /api/chats status 200': (r) => r.status === 200,
		});
		if (!chatOk) ok = false;
	} else {
		const content = `k6 ${__VU}-${__ITER}-${Date.now()}`;
		const wsUrl = wsUrlFromHttpBase(baseUrl);
		const stompOk = runChatSendStomp(wsUrl, wsAuthHeaders(), data.chatRoomId, data.userId, content);
		const chatOk = check(stompOk, {
			'STOMP /app/chat/send (메시지 전송) 성공': (v) => v === true,
		});
		if (!chatOk) ok = false;
	}

	const resUrl = reservationUrl();
	const resName =
		reservationRole === 'owner'
			? 'GET /api/owners/reservations/summary'
			: 'GET /api/childrens/reservations';
	const bookingRes = get(resUrl, resName);
	const bookingOk = check(bookingRes, {
		[`${resName} status 200`]: (r) => r.status === 200,
	});
	if (!bookingOk) ok = false;

	if (ok) {
		successCounter.add(1);
	} else {
		failureCounter.add(1);
	}

	sleep(sleepSec);
}

export function handleSummary(data) {
	const html = buildHtmlReport(data);
	const text = textSummary(data, { indent: ' ', enableColors: false });

	console.log('\n=== onharu 도메인 (채팅 + 예약) k6 ===');
	console.log(text);

	return {
		stdout: text,
		'report-last.html': html,
	};
}
