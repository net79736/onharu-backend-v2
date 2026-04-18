/**
 * Spring STOMP 채팅 전송 (/app/chat/send) — k6 WebSocket + STOMP 1.2 텍스트 프레임
 * @see ChatStompDestination#appDestinationChatSend
 */
import ws from 'k6/ws';

/**
 * @param {string} httpBase 예: http://localhost:8080
 * @returns {string} ws://.../ws-chat
 */
export function wsUrlFromHttpBase(httpBase) {
	const trimmed = httpBase.replace(/\/$/, '');
	if (/^https/i.test(trimmed)) {
		return trimmed.replace(/^https/i, 'wss') + '/ws-chat';
	}
	return trimmed.replace(/^http/i, 'ws') + '/ws-chat';
}

/**
 * STOMP SEND 로 채팅 메시지 1건 전송. WebSocket 핸드셰이크 ~ 종료까지 블로킹.
 * @returns {boolean} 전송까지 성공하면 true
 */
export function runChatSendStomp(wsUrl, wsHeaderParams, chatRoomId, senderId, content) {
	let success = false;
	let sent = false;

	const params = {
		headers: {
			...wsHeaderParams,
			'Sec-WebSocket-Protocol': 'v10.stomp,v11.stomp,v12.stomp',
		},
		tags: { name: 'STOMP /app/chat/send' },
	};

	const res = ws.connect(wsUrl, params, function (socket) {
		socket.on('open', function () {
			socket.send('CONNECT\naccept-version:1.2\nheart-beat:0,0\n\n\x00');
		});

		socket.on('message', function (data) {
			const text =
				typeof data === 'string'
					? data
					: String.fromCharCode.apply(null, new Uint8Array(data));

			if (text.indexOf('ERROR') !== -1) {
				success = false;
				socket.close();
				return;
			}

			if (!sent && text.indexOf('CONNECTED') !== -1) {
				sent = true;
				const payload = JSON.stringify({
					chatRoomId: chatRoomId,
					senderId: senderId,
					content: content,
				});
				socket.send(
					'SEND\ndestination:/app/chat/send\ncontent-type:application/json\n\n' +
						payload +
						'\x00'
				);
				success = true;
				socket.setTimeout(function () {
					socket.close();
				}, 600);
			}
		});

		socket.on('error', function () {
			success = false;
			try {
				socket.close();
			} catch (e) {}
		});

		socket.setTimeout(function () {
			try {
				socket.close();
			} catch (e) {}
		}, 20000);
	});

	if (!res || res.status !== 101) {
		return false;
	}
	return success;
}
