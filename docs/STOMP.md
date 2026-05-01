# STOMP(WebSocket) 채팅 인터페이스

실시간 전송 경로를 STOMP 관점에서 정리한다. 메시지가 브로커로 넘어간 뒤의 **후처리·부하 회고** 는 [`docs/RABBITMQ.md`](./RABBITMQ.md) 에, **Kafka·아웃박스** 적재 흐름은 [`docs/chat-kafka-flow.md`](./chat-kafka-flow.md) 에 있다.

---

## 1. Chat REST — `ChatControllerImpl` 베이스 `/api/chats`

| Method | Path | 설명 |
|--------|------|------|
| `POST` | `/api/chats` | 채팅방 생성 |
| `POST` | `/api/chats/{chatRoomId}/participants` | 초대 |
| `PATCH` | `/api/chats/{chatRoomId}` | 방 이름 수정 |
| `POST` | `/api/chats/{chatRoomId}/read` | 읽음 처리 |
| `GET` | `/api/chats` | 내 채팅방 목록 |
| `GET` | `/api/chats/{chatRoomId}/messages` | 채팅방 메시지 조회 |
| `DELETE` | `/api/chats/{chatRoomId}` | 퇴장 |
| `POST` | `/api/chats/{chatRoomId}` | 입장 |

---

## 2. STOMP 주소 규약 (온하루)

| prefix | 용도 | 예시 |
|---|---|---|
| **`/app/**`** | 클라이언트 → 서버 전송 (`@MessageMapping` 대상) | `/app/chat/send` |
| **`/topic/**`** | 서버 → 구독자 브로드캐스트 (브로커 경유) | `/topic/chat/{chatRoomId}` |
| **`/queue/**`** | 서버 → 특정 사용자 큐 (예약) | `/queue/...` |

브로커 prefix 는 `ChatStompDestination` 에 상수로 집중돼 있고, `WebSocketConfig.configureMessageBroker` 가 prefix 를 등록한다.

```js
// 클라이언트 예시
const socket = new WebSocket("ws://localhost:8080/ws-chat");
const stompClient = Stomp.over(socket);

stompClient.connect({}, (frame) => {
  // 특정 채팅방 구독
  stompClient.subscribe('/topic/chat/31', (message) => {
    showGreeting(JSON.parse(message.body));
  });

  // 메시지 발행
  stompClient.publish({
    destination: '/app/chat/send',
    body: JSON.stringify({ chatRoomId: 31, senderId: 733, content: '안녕' })
  });
});
```

---

## 3. 두 가지 브로커 모드

`WebSocketConfig` 가 `onharu.stomp.relay.enabled` 로 분기한다.

### (a) In-memory SimpleBroker (기본, 단일 JVM)
```
클라이언트 ──SEND──► [Spring SimpleBroker] ──브로드캐스트──► 구독자들
                     (같은 JVM 메모리)
```
- 구성 단순, 외부 의존성 0
- **단일 인스턴스 한정** — 스케일아웃 불가

### (b) RabbitMQ STOMP Broker Relay (프로덕션)
```
[앱 #1] ─┐
[앱 #2] ─┼──► RabbitMQ STOMP Plugin (61613) ──► 어느 인스턴스에 붙었든 모든 구독자
[앱 #N] ─┘
```
- `onharu.stomp.relay.enabled=true` 필요
- **수평 확장 가능**, 관리 UI 로 구독/처리량 관측
- RabbitMQ 미기동 시 앱 기동 실패 → dev 에서는 `false` 권장. 자세한 책임 분담·운영은 [`docs/RABBITMQ.md`](./RABBITMQ.md).

---

## 4. 코드 흐름 — 프론트 기준

```
모달 열기
  └─ loadChatRooms()          ← GET /api/chats (목록)
       └─ 채팅방 클릭
            └─ openChatRoom()
                 ├─ loadMessages()     ← GET /api/chats/{id}/messages (REST 커서)
                 │    └─ 스크롤 상단 도달 시 더 불러오기
                 └─ connectStomp()     ← ws:// 연결
                      └─ subscribe /topic/chat/{id}
                           └─ 새 메시지 수신 → renderMessages()

전송 버튼 / Enter
  └─ sendChatMessage()
       └─ stompClient.publish /app/chat/send
```

---

## 5. 서버 처리 파이프라인 — `/app/chat/send` 한 프레임이 지나는 길

```
k6 / 브라우저
     │  STOMP SEND
     ▼
[Spring clientInboundChannel 스레드 풀]
     │  @MessageMapping("/chat/send")
     ▼
ChatMessageStompHandler.sendMessage
     │
     ▼
ChatFacade.createChatMessage  ── @Transactional ──┐
     │                                            │
     │  INSERT chat_messages                      │
     │  UPDATE chat_participants (원자 UPDATE)    │ 메인 TX
     │  INSERT outbox_events (옵션)               │
     │  eventPublisher.publishEvent(...)          │
     │                                          COMMIT
     │                                            │
     ▼                                            │
SimpMessagingTemplate.convertAndSend              │
     │  → /topic/chat/{chatRoomId} 으로 브로드캐스트│
     │    (SimpleBroker 또는 RabbitMQ relay)       │
     │                                            │
     ▼                                            ▼
   구독자들                                ChatMessagePublishedListener (비동기)
                                          ├─ UPDATE chat_rooms.last_message_id
                                          └─ RabbitMQ onharu.chat.events 큐 발행
```

핵심: **메인 `@Transactional` 내부는 `chat_messages` INSERT 경로만 남겼다.** 동일 `chat_rooms` 행에 대한 UPDATE 와 외부 브로커 발행은 AFTER_COMMIT + `@Async` 로 밀어내 **S→X lock 승격 데드락을 구조적으로 제거**했다. 자세한 배경은 [RABBITMQ.md §3–§5](./RABBITMQ.md#3-그런데--왜-느려졌나).

---

## 6. STOMP 로그 (프레임 생명주기)

![이미지](./images/websocket_1.png)

### 주요 명령어
| Command | 의미 |
|---|---|
| `↑ CONNECT` | 클라이언트가 서버에 첫 인사 (handshake 후 STOMP 세션 시작) |
| `↓ CONNECTED` | 서버가 세션 승인 (로그인 사용자 정보 포함) |
| `↑ SUBSCRIBE` | 클라이언트가 `/topic/chat/{id}` 구독 시작 |
| `↑ SEND` | 클라이언트가 `/app/chat/send` 로 메시지 발행 |
| `↓ MESSAGE` | 서버가 `/topic/chat/{id}` 구독자에게 배달 완료 |

### Pub-Sub 흐름
1. **SUBSCRIBE**: 방 입장 → 수신 대기
2. **SEND**: 발신자가 서버로 메시지 publish
3. **Spring 처리**: DB 저장 → `chatMessageId` 발급
4. **MESSAGE**: `/topic/chat/{id}` 구독자 전원에게 JSON 배달

### 수신 JSON 구조

| 필드 | 의미 | 예시 |
| :--- | :--- | :--- |
| `chatMessageId` | DB 저장 메시지 고유 ID | `419` |
| `sender` | 발신자 사용자 ID | `733` |
| `content` | 메시지 본문 | `"안돼"` |
| `createdAt` | 서버 기록 시각 | `2026-04-17T…` |

---

## 7. ⚠️ STOMP SEND 는 fire-and-forget 이다

클라이언트가 `SEND` 프레임을 쏘고 나면 **서버 처리 성공 여부와 무관하게 해당 호출은 "보냈음" 으로 기록**된다. 즉:

- 서버 내부에서 DB 데드락/예외 발생 → 클라이언트는 모름
- k6 부하 테스트에서 "성공률 100 %" 가 나와도 **실제 DB 커밋 여부는 별도 검증** 필요
- 검증 방법:
  ```sql
  SELECT COUNT(*) FROM chat_messages WHERE content LIKE 'k6 %';
  ```
  이 수치의 증가분이 k6 `iterations` 수와 일치하는지 비교

이 성질 때문에 2026-04 부하테스트에서 "성공 100 %" 라는 잘못된 신호가 데드락을 감췄다. 반드시 **DB 커밋 수 + `SHOW ENGINE INNODB STATUS`** 를 같이 본다.

---

## 8. 부하 상황 진단 체크리스트 (STOMP 관점)

```
증상: k6 성공률이 낮거나 iteration_duration 이 비정상적으로 김
   │
   ├─ [1] DB 커밋 수 검증
   │       SELECT COUNT(*) FROM chat_messages WHERE content LIKE 'k6 %';
   │       → k6 iterations 와 차이 있으면 "유실" = 서버측 에러/롤백 있음
   │
   ├─ [2] InnoDB 데드락 카운터
   │       SHOW ENGINE INNODB STATUS \G
   │       → LATEST DETECTED DEADLOCK 타임스탬프가 부하 시간대에 있으면 락 경합
   │
   ├─ [3] JVM 스레드 덤프
   │       jstack <pid>  (clientInboundChannel-*, chat-event-* 스레드 상태)
   │       → parking 위치가 HikariPool.getConnection = 커넥션 풀 포화
   │       → parking 위치가 RabbitTemplate = 브로커 응답 대기
   │
   └─ [4] HikariCP 풀 수
         docker exec coupon-mysql mysql -e "SELECT COUNT(*) FROM information_schema.processlist WHERE db='onharu';"
         → 피크에서 maximum-pool-size 에 도달했으면 풀 확대 검토
```

---

## 9. 관련 파일

| 역할 | 경로 |
|---|---|
| STOMP 엔드포인트·브로커 설정 | `src/main/java/com/backend/onharu/config/WebSocketConfig.java` |
| `/app/chat/send` 핸들러 | `src/main/java/com/backend/onharu/infra/websocket/ChatMessageStompHandler.java` |
| 주소 상수 | `src/main/java/com/backend/onharu/domain/support/ChatStompDestination.java` |
| 채팅 Facade | `src/main/java/com/backend/onharu/application/ChatFacade.java` |
| AFTER_COMMIT 이벤트 | `src/main/java/com/backend/onharu/event/model/ChatMessagePublishedEvent.java` |
| 비동기 리스너 | `src/main/java/com/backend/onharu/event/listener/ChatMessagePublishedListener.java` |
| 비동기 executor | `src/main/java/com/backend/onharu/config/AsyncConfig.java` |
| HikariCP 튜닝 | `src/main/resources/application-dev.yaml` (`spring.datasource.hikari.*`) |
| 부하 스크립트 | `load-test/stomp-chat-send.js`, `load-test/onharu-domains-load.js` |
