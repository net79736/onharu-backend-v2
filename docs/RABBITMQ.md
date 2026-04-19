# RabbitMQ (선택)

로컬 학습용 예제(`rabbitmq` 큐 + `basicPublish`)를 Spring AMQP 로 옮긴 형태입니다.

## 켜는 방법

1. **Docker**: `docker compose up -d rabbitmq` — AMQP `5672`, 관리 UI `15672`  
   - 기본 계정: `onharu` / `onharu` (`docker-compose.yml` · `application-rabbitmq.yaml` 과 맞춤)
2. **설정**: `application-dev.yaml` 에서 `optional:application-rabbitmq.yaml` 로드됨  
3. **환경변수**: `ONHARU_RABBITMQ_ENABLED=true` (또는 `onharu.rabbitmq.enabled=true`)

호스트에서 앱만 실행할 때: `RABBITMQ_HOST=localhost`  
`docker compose` 의 `onharu` 서비스에서 실행할 때: `RABBITMQ_HOST=rabbitmq` (compose에 기본값 반영)

## 동작

- 채팅 메시지 저장 직후 `ChatFacade`가 `ChatRabbitPublishPort` → 큐 `onharu.chat.events` 로 JSON 전송  
- `OnharuChatEventsRabbitListener` 가 동일 큐를 구독해 로그 출력 (워커·후처리 확장 전 단계)

Kafka·아웃박스와 **독립**입니다. 둘 다 켜면 같은 채팅 이벤트가 Kafka(아웃박스 경로)와 RabbitMQ 양쪽에 갈 수 있습니다.

## 관련 코드

| 역할 | 위치 |
|------|------|
| 포트 | `domain.event.ChatRabbitPublishPort` |
| 발행 | `infra.rabbitmq.ChatRabbitPublishAdapter` |
| 구독 | `infra.rabbitmq.OnharuChatEventsRabbitListener` |
| 연결·큐 | `config.RabbitMqConfig` |
