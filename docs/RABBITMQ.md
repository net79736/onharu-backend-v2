# RabbitMQ — 도입 배경, 진화, 운영

온하루 채팅에서 RabbitMQ 는 두 가지 역할을 맡는다.

1. **STOMP Broker Relay** — WebSocket 채팅 프레임의 브로드캐스트 (수평 확장 용).
2. **AMQP 이벤트 큐 `onharu.chat.events`** — 채팅 이벤트 후처리 훅 (알림/통계/검색 인덱서 등).

STOMP 클라이언트 시점 흐름은 [`docs/STOMP.md`](./STOMP.md), Kafka 아웃박스와의 관계는 [`docs/chat-kafka-flow.md`](./chat-kafka-flow.md) 를 참고.

---

## 1. 켜는 방법

1. **Docker**: `docker compose up -d rabbitmq`
   - AMQP `5672`, 관리 UI `15672`, STOMP 플러그인 `61613`
   - 기본 계정: `onharu` / `onharu` (`docker-compose.yml` · `application-rabbitmq.yaml` 과 맞춤)
2. **설정 로드**: `application-dev.yaml` 에서 `optional:application-rabbitmq.yaml` 을 import
3. **활성화 플래그**
   - `onharu.rabbitmq.enabled=true` → AMQP publisher/listener 빈 등록 + `RabbitAutoConfiguration` 활성
   - `onharu.stomp.relay.enabled=true` → WebSocket 브로커를 RabbitMQ STOMP 플러그인 relay 로 전환

| 실행 환경 | `RABBITMQ_HOST` |
|---|---|
| 호스트에서 앱만 실행 | `localhost` |
| `docker compose` 내 `onharu` 서비스 | `rabbitmq` (compose 기본값) |

두 플래그가 꺼지면 RabbitMQ 가 없어도 앱은 부팅된다 (단일 JVM 한정 기능으로 동작).

---

## 2. 왜 도입했나 — SimpleBroker 의 한계

초기 채팅은 Spring 기본 **in-memory SimpleBroker** 를 썼다.

```
[앱 #1 JVM]
  클라이언트 A ──SEND──► SimpleBroker ──브로드캐스트──► 클라이언트 B (같은 JVM 한정)
```

간단하고 빠르지만 한 JVM 에 갇힌다.

| 한계 | 구체적 문제 |
|---|---|
| **수평 확장 불가** | 인스턴스 2 개 이상 배포 시 A 서버 사용자의 메시지가 B 서버 사용자에게 전달되지 않음 |
| **영속성 없음** | 앱 재시작 시 진행 중 구독/전송 소실 |
| **후처리 훅 포인트 없음** | 채팅 이벤트를 알림·통계 모듈이 받으려면 도메인 코드가 직접 호출 → 강결합 |
| **재시도 정책 없음** | 소비자 로직 일시 실패 시 재시도/DLQ 없음 |
| **관측성 제로** | 처리량·지연·drop 측정 불가 |

이 시점에 RabbitMQ 를 **두 갈래**로 도입했다.

---

## 3. 도입으로 얻은 것

### (a) STOMP Broker Relay — 실시간 전파
```
[앱 #1] ──┐
[앱 #2] ──┼──► RabbitMQ STOMP Plugin (61613) ──► 어느 JVM 에 붙은 구독자든 전파
[앱 #N] ──┘
```

- ✅ **N 인스턴스 수평 확장**
- ✅ **무중단 배포** — 구독 상태가 브로커에 있어 인스턴스 교체 중에도 drop 최소
- ✅ **Management UI 관측**

설정: `config/WebSocketConfig.java` 의 `enableStompBrokerRelay(...)` (`onharu.stomp.relay.enabled` 분기).

### (b) AMQP 이벤트 큐 `onharu.chat.events` — 후처리 훅

```
ChatFacade.createChatMessage
   ├─ STOMP /topic/chat/{id}  : 구독자에게 실시간 전파
   └─ AMQP onharu.chat.events : 소비자들이 각자 구독
                                 ├─ 알림 서비스
                                 ├─ 통계 집계
                                 └─ 검색 인덱서 …
```

- ✅ **느슨한 결합**: 새 후처리 모듈 추가 시 큐 구독만 추가. 도메인 코드 변경 없음
- ✅ **재시도 + DLQ**: 수동 ACK + `onharu.chat.dlx` 로 영구 실패 격리
  - `RabbitMqConfig.rabbitListenerContainerFactory` → `AcknowledgeMode.MANUAL`
  - 정상 처리 시 `basicAck`, JSON 파싱 실패 → `basicNack(requeue=false)` → DLQ, 처리 중 예외 → `basicNack(requeue=true)` → 재시도
- ✅ **영속성**: durable queue + `MessageDeliveryMode.PERSISTENT` — 브로커 재시작에도 메시지 보존

Kafka 아웃박스와는 **독립**이다. 둘 다 켜면 같은 채팅 이벤트가 Kafka 와 RabbitMQ 양쪽으로 흘러간다 (목적이 다른 다운스트림을 동시에 먹이는 설계).

관련 코드

| 역할 | 위치 |
|---|---|
| 포트 | `domain.event.ChatRabbitPublishPort` |
| 발행 (AFTER_COMMIT + @Async 에서 호출) | `infra.rabbitmq.ChatRabbitPublishAdapter` |
| 구독 (수동 ACK) | `infra.rabbitmq.OnharuChatEventsRabbitListener` |
| 연결·큐·DLX 토폴로지 | `config.RabbitMqConfig` |

---

## 4. 그런데 — 왜 느려졌나 (사건 회고)

RabbitMQ 도입 직후 k6 부하 (100 VU × 30 iter = 3,000 요청) 측정치:

| 단계 | iteration_duration avg | 총 소요 | STOMP 성공률 |
|---|---|---|---|
| SimpleBroker (baseline) | **683 ms** | 21 초 | 100 % |
| **RabbitMQ 도입 직후** | **30,370 ms (44×↑)** | **15 분 35 초** | **28 %** 🚨 |

**원인은 RabbitMQ 자체가 아니라 "어떻게 호출했는가"** 였다. 두 단계에 걸쳐 드러났다.

### 4-1. 1단계: MySQL InnoDB 데드락

초기 `ChatFacade.createChatMessage` 는 한 `@Transactional` 안에서 **같은 `chat_rooms` 행을 두 번** 건드렸다.

```java
@Transactional
public ChatMessageResponse createChatMessage(...) {
    INSERT chat_messages (chat_room_id = X, ...)   // (1) FK 체크 → chat_rooms.id=X 에 S-lock
    chatRoom.updateLastMessage(messageId);          // (2) flush 시 같은 행에 X-lock 승격 필요
    rabbitTemplate.convertAndSend(...);             // (3) 트랜잭션 안에서 네트워크 I/O
}
```

100 VU 가 동일 방에 쏘면서 **S → X 승격 데드락**이 꾸준히 발생:

```
Thread A ─ S-lock on chat_rooms.id=33 ─ wants X-lock ┐
Thread B ─ S-lock on chat_rooms.id=33 ─ wants X-lock ┤ 원형 대기 → DEADLOCK
Thread C ─ S-lock on chat_rooms.id=33 ─ wants X-lock ┘
```

`SHOW ENGINE INNODB STATUS` 의 `LATEST DETECTED DEADLOCK` 에 이 패턴이 정확히 찍혔다. MySQL 이 한쪽을 강제 롤백 → `chat_messages` INSERT 가 **조용히 유실**. STOMP SEND 는 fire-and-forget 이라 k6 는 "성공" 으로 잘못 카운트했다.

### 4-2. 2단계: 데드락 수정 → 커넥션 풀 포화

1차 수정으로 동일 행 재접근을 트랜잭션 밖으로 분리했다 — `@TransactionalEventListener(AFTER_COMMIT) + @Transactional(REQUIRES_NEW)`. 데드락은 완전히 사라졌지만 **더 느려졌다 (15 분 35 초)**. 이유:

```
[요청 스레드 = clientInboundChannel]
  메인 @Transactional
     ├─ DB 커넥션 #1 획득
     │  INSERT chat_messages / UPDATE chat_participants / INSERT outbox_events
     │  COMMIT + 커넥션 #1 반납
     │
  AFTER_COMMIT 리스너 (동기, 같은 스레드)
     ├─ @Transactional(REQUIRES_NEW)
     │  ├─ DB 커넥션 #2 획득 ← 풀에서 또 대기
     │  │  UPDATE chat_rooms SET last_message_id = ...
     │  └─ rabbitTemplate.convertAndSend(...) ← 브로커 ACK 대기
     │  COMMIT + 커넥션 #2 반납
```

요청 1건이 **DB 커넥션을 2 회 순차 점유** + **RabbitMQ 네트워크 왕복**을 호출 스레드가 끝까지 기다렸다.

그리고 결정타:

```yaml
# 수정 전 application-dev.yaml 에 hikari 설정이 전혀 없었음 → Boot 기본값 적용
```

Spring Boot 기본값은 `maximumPoolSize = 10`, `connectionTimeout = 30,000 ms`. 100 VU 가 들어오면 10 개가 즉시 점유 → 나머지 ~70 스레드가 풀에서 대기 → **정확히 30 초 뒤 타임아웃**.

스레드 덤프가 직설적으로 증언:

```
"clientInboundChannel-3" TIMED_WAITING (parking)
  at com.zaxxer.hikari.util.ConcurrentBag.borrow(ConcurrentBag.java:151)
  at com.zaxxer.hikari.pool.HikariPool.getConnection(HikariPool.java:162)
  at org.hibernate.engine.jdbc.connections.internal.DatasourceConnectionProviderImpl.getConnection
  at com.backend.onharu.event.listener.ChatMessagePublishedListener$$SpringCGLIB$$0.handleChatMessagePublished
```

k6 측 `http_req_duration max = 30,000 ms` 는 HikariCP `connectionTimeout` 과 **바이트 단위로 일치** — 스모킹건.

> **비유**: 은행 창구가 10 개뿐인데 100 명이 한꺼번에 와서 각자 "본 업무 + 추가 업무" 로 창구를 두 번씩 쓰겠다고 하면, 대부분은 번호표만 뽑고 30 분 기다리다 포기.

---

## 5. 해결 — 세 가지 변경

### (1) 동일 행 재접근 제거 (트랜잭션 구조)
`ChatFacade.createChatMessage` 에서 `chat_rooms` UPDATE 와 RabbitMQ publish 를 **AFTER_COMMIT 이벤트 리스너로 밀어냈다**. 메인 트랜잭션은 `INSERT chat_messages` + 참가자 읽음 원자 UPDATE + outbox 만 수행.

### (2) 리스너 비동기화 (`@Async`)
`ChatMessagePublishedListener.handleChatMessagePublished` 에 `@Async("chatEventExecutor")` 추가. 호출 스레드는 이벤트 publish 직후 즉시 리턴. 리스너는 별도 풀(`chat-event-*`)에서 백그라운드로 `chat_rooms` UPDATE + Rabbit publish 수행.

### (3) 전용 executor 빈 등록 + HikariCP 풀 확대
`AsyncConfig` 에 `ThreadPoolTaskExecutor` (core 8 / max 32 / queue 500 / `CallerRunsPolicy`). `application-dev.yaml` 에 `maximum-pool-size: 30, minimum-idle: 10, connection-timeout: 10_000`.

### 수정 후 실행 흐름
```
[clientInboundChannel 스레드]
  메인 @Transactional
    ├─ DB 커넥션 획득
    │   INSERT chat_messages / UPDATE chat_participants / INSERT outbox_events
    │  COMMIT + 커넥션 반납
    └─ publishEvent(...)           ← 즉시 리턴

   ★ 호출 스레드는 여기서 끝. 다음 STOMP 프레임 즉시 처리.

[chat-event-* 스레드 (별도 풀)]
  @Async 리스너
     ├─ @Transactional(REQUIRES_NEW)
     │  ├─ DB 커넥션 획득
     │  │  UPDATE chat_rooms
     │  └─ rabbitTemplate.convertAndSend(...)
     │  COMMIT
```

**요청당 호출 스레드 점유 커넥션: 2 → 1** 로 반감. RabbitMQ 브로커가 느려져도 채팅 전송 레이턴시에 반영되지 않는다.

---

## 6. 실측 비교 — 3 단계

동일 k6 시나리오 (100 VU × 30 iter, 동일 `chatRoomId`):

| 지표 | 원본 (데드락) | 1차 수정 (동기 AFTER_COMMIT) | **최종 (+ @Async + 풀 튜닝)** |
|---|---|---|---|
| **총 소요 시간** | 21 초 | **15 분 35 초** | **20.8 초** ✅ |
| **iteration_duration avg** | 683 ms | 30,370 ms | **683.82 ms** ✅ |
| **http_req_duration p95** | 78 ms | 29,560 ms | **80.83 ms** ✅ |
| **http_req_duration max** | 332 ms | 30,000 ms (풀 timeout) | **207 ms** ✅ |
| **STOMP send 성공률** | 100 % (유실) | 28 % | **100 %** ✅ |
| **DB 실제 커밋 증가분** | +3000 (일부 롤백) | +323 | **+3000 정확** ✅ |
| **MySQL `onharu` 커넥션 피크** | 10/10 포화 | 10/10 포화 | **23/30 여유** ✅ |
| **신규 데드락** | **발생** 🚨 | 없음 | **없음** ✅ |

### 로그 스케일 그래프

```
iteration_duration (ms)

SimpleBroker (baseline)    ▓▓                    683
RabbitMQ + 데드락          ▓▓                    683  (실제 유실 있음)
1차 수정 (동기 리스너)     ▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓▓    30,370   ← 44× 악화
최종 (@Async + 풀 튜닝)    ▓▓                    683.82   ← baseline 복귀
```

**핵심**: RabbitMQ 를 유지한 채 baseline 속도로 완전히 복귀. 도입 이점 (수평 확장·후처리 훅·DLQ·영속성) 은 전부 그대로.

---

## 7. 지금 시점의 역할 요약

| 영역 | SimpleBroker 시대 | 현재 (RabbitMQ + 최적화 완료) |
|---|---|---|
| STOMP 처리 속도 | 수 ms | **수 ms (동일)** |
| 수평 확장 | 불가 | **N 인스턴스** |
| 후처리 훅 | 없음 | **`onharu.chat.events` 큐 구독만으로 추가** |
| 재시도 + DLQ | 없음 | **있음** (수동 ACK + `onharu.chat.dlx`) |
| 영속성 | 없음 | **durable queue + persistent 메시지** |
| 데드락 내성 | 취약 | **구조적으로 불가능** (같은 tx 에서 동일 행 재접근 없음) |
| 관측성 | 없음 | **Management UI + k6 리포트 + 표준 진단 루틴** |

---

## 8. 운영 · 진단 체크리스트

### 브로커 상태
- Management UI `http://localhost:15672/#/queues` → `onharu.chat.events` 큐 깊이, in/out rate 확인
- `onharu.chat.dead-letter` 큐에 메시지가 쌓이면 파싱 실패 또는 영구 실패 케이스 축적 — 로그에서 `RabbitMQ 채팅 이벤트 JSON 파싱 불가` 검색

### 지연 체감 시
1. `jstack <pid>` 로 `chat-event-*` 스레드가 대기 중인 위치 확인
   - `rabbitTemplate.convertAndSend` → 브로커 응답 지연
   - `HikariPool.getConnection` → 풀 소진 (비정상, 풀 확대 검토)
2. DB 커넥션 수
   ```
   SELECT COUNT(*) FROM information_schema.processlist WHERE db='onharu';
   ```
3. 데드락 발생 여부
   ```
   SHOW ENGINE INNODB STATUS \G   -- LATEST DETECTED DEADLOCK 섹션 확인
   ```

### 부하 테스트 검증
STOMP SEND 는 fire-and-forget 이라 **성공률만으로 판단 금지**. k6 실행 전/후 DB 커밋 수를 직접 비교:
```sql
SELECT COUNT(*) FROM chat_messages WHERE content LIKE 'k6 %';
```
이 값의 증가분이 k6 `iterations` 와 일치하지 않으면 서버측 유실이다.

---

## 9. 설정 레퍼런스

### `application-rabbitmq.yaml`
```yaml
onharu:
  rabbitmq:
    enabled: ${ONHARU_RABBITMQ_ENABLED:true}
    chat-events-queue: ${ONHARU_RABBITMQ_CHAT_QUEUE:onharu.chat.events}
  stomp:
    relay:
      enabled: ${ONHARU_STOMP_RELAY_ENABLED:true}
      host: ${RABBITMQ_STOMP_HOST:localhost}
      port: ${RABBITMQ_STOMP_PORT:61613}

spring:
  rabbitmq:
    host: ${RABBITMQ_HOST:localhost}
    port: ${RABBITMQ_PORT:5672}
    username: ${RABBITMQ_USERNAME:onharu}
    password: ${RABBITMQ_PASSWORD:onharu}
    listener:
      simple:
        acknowledge-mode: manual       # 처리 완료 후 명시적 basicAck
      retry:
        enabled: true
        max-attempts: 3
        initial-interval: 1000ms
      default-requeue-rejected: false  # 재시도 끝나면 DLQ 로
```

### `application-dev.yaml` HikariCP (수정 후)
```yaml
spring:
  datasource:
    hikari:
      maximum-pool-size:  ${SPRING_DATASOURCE_HIKARI_MAX:30}
      minimum-idle:       ${SPRING_DATASOURCE_HIKARI_MIN_IDLE:10}
      connection-timeout: ${SPRING_DATASOURCE_HIKARI_CONN_TIMEOUT:10000}
      pool-name:          OnharuHikari
```

---

## 10. 핵심 교훈

1. **브로커 도입은 "전파 경로만" 바꾸는 일이 아니다.** Rabbit publish 를 어디서 부르는지 (트랜잭션 안/밖, 동기/비동기) 가 성능을 수 배 좌우한다.
2. **`@TransactionalEventListener(AFTER_COMMIT)` 만으로는 부족하다.** 기본 동기 실행이라 호출 스레드가 끝까지 기다린다. DB + 네트워크 I/O 가 섞인 리스너는 `@Async` 로 별도 풀에서 돌리는 것이 사실상 필수.
3. **Spring Boot HikariCP 기본값 (10 / 30 s) 은 부하테스트 환경에서 거의 항상 부족하다.** *예상 동시 요청 × 요청당 평균 커넥션 점유 수* 로 산출해 YAML 에 명시.
4. **데드락·락 대기는 "전체 처리량" 지표로 안 잡힌다.** `SHOW ENGINE INNODB STATUS` + `Innodb_row_lock_waits` + `jstack` 세 트랙을 같이 본다.
5. **STOMP SEND 는 fire-and-forget** 이다. 서버측 실패를 클라이언트가 알 수 없으므로 부하테스트 검증은 반드시 **DB 실제 커밋 수**로 교차 확인.

---

## 11. 관련 파일 인덱스

| 역할 | 경로 |
|---|---|
| 연결·큐·DLX 토폴로지 | `src/main/java/com/backend/onharu/config/RabbitMqConfig.java` |
| STOMP Broker Relay 설정 | `src/main/java/com/backend/onharu/config/WebSocketConfig.java` |
| 이벤트 publisher 포트 | `src/main/java/com/backend/onharu/domain/event/ChatRabbitPublishPort.java` |
| Publisher 어댑터 | `src/main/java/com/backend/onharu/infra/rabbitmq/ChatRabbitPublishAdapter.java` |
| Listener (수동 ACK) | `src/main/java/com/backend/onharu/infra/rabbitmq/OnharuChatEventsRabbitListener.java` |
| AFTER_COMMIT 이벤트 | `src/main/java/com/backend/onharu/event/model/ChatMessagePublishedEvent.java` |
| 비동기 리스너 (chat_rooms bump + rabbit publish) | `src/main/java/com/backend/onharu/event/listener/ChatMessagePublishedListener.java` |
| 비동기 executor | `src/main/java/com/backend/onharu/config/AsyncConfig.java` |
| HikariCP 튜닝 | `src/main/resources/application-dev.yaml` |
| RabbitMQ 프로파일 | `src/main/resources/application-rabbitmq.yaml` |
| 부하 테스트 | `load-test/run.sh`, `load-test/onharu-domains-load.js`, `load-test/stomp-chat-send.js` |
