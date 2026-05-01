# CLAUDE.md

이 저장소에서 코드를 다룰 때 Claude Code(claude.ai/code)를 위한 안내입니다.

## 빌드 / 실행 / 테스트

Java 17(Spring Boot 3.3.4) 기준 Gradle Wrapper를 사용합니다.

```bash
./gradlew build                         # 전체 빌드(테스트 + jacocoTestReport 실행)
./gradlew bootRun                       # 로컬 실행(기본 프로필 dev)
./gradlew test                          # 테스트만 — spring.profiles.active=test 강제(H2 + embedded-redis)
./gradlew test --tests "FQCN"           # 단일 테스트 클래스
./gradlew test --tests "FQCN.method"    # 단일 테스트 메서드
./gradlew jacocoTestReport              # HTML: build/reports/jacoco/test/html/index.html
./gradlew sonarqube                     # SONAR_HOST_URL, SONAR_TOKEN 환경 변수 필요
```

MySQL, Redis, RabbitMQ, Kafka+ZK, MinIO, SonarQube, Jenkins 등 인프라는 `docker-compose up -d`로 띄웁니다. 개발 환경(`application-dev.yaml`)에서는 MySQL이 compose 기본 3306이 아니라 **3307**에 노출됩니다 — compose에서 필요한 서비스만 켜거나 `SPRING_DATASOURCE_URL`을 덮어쓰세요.

k6 부하는 `load-test/`에 있으며 `./load-test/run.sh`로 실행합니다. `K6_COOKIE`/`K6_BEARER_TOKEN`이 없으면 공개 스모크 스크립트가 돌고, 둘 중 하나라도 있으면 인증 도메인 스크립트가 실행되며 `report-last.html`이 열립니다.

## 아키텍처

`com.backend.onharu` 아래에 헥사고날에 가까운 레이어링이 있습니다.

- `interfaces/api/controller/` — `I*Controller` 인터페이스 + `impl/*ControllerImpl`로 분리. REST 경로는 모두 `/api/...`. `interfaces/api/support/ApiControllerAdvice`에서 전역 예외 처리로 `CoreException`(`ErrorType` 경유)을 400/401/403/404/409/500에 매핑합니다.
- `application/*Facade.java` — 여러 도메인 서비스에 걸친 트랜잭션 유스케이스 조합. 컨트롤러는 도메인 서비스가 아니라 파사드를 호출합니다.
- `domain/<aggregate>/` — 엔티티, enum, 도메인 서비스(`*CommandService` / `*QueryService`), 저장소 **인터페이스**(포트). 엔티티는 `common/BaseEntity`를 상속합니다. 오류는 `support.error.CoreException` + `ErrorType`.
- `infra/` — 어댑터: `db/`(JPA 엔티티 + `*RepositoryImpl`), `redis/`, `security/`(OAuth2 + 세션), `kafka/`, `rabbitmq/`, `websocket/`, `email/`, `nts/`.
- `event/` — 프로세스 내 `ApplicationEventPublisher` 흐름(예약 알림 등). `domain/event/`와는 별개이며, 여기에는 **메시징 포트**(`ChatKafkaOutboxPort`, `ChatRabbitPublishPort`)가 있습니다.
- `interfaces/shceduler/` — `@Scheduled` 진입점. 패키지명 오타(`shceduler`)는 의도적/부하를 고려한 이름이므로, 참조를 함께 옮기지 않고 "수정"하지 마세요.

### 메시징 — Kafka와 RabbitMQ는 둘 다 선택 사항

브로커 둘 다 플래그로 막아서 없어도 앱이 기동되게 합니다.

- `onharu.kafka.enabled` (기본 **false**) — `false`이면 `KafkaAutoConfiguration`이 제외되고 `config.KafkaConfig`가 등록되지 않습니다. dev에서는 `application-kafka.yaml`이 `optional:`로 import됩니다.
- `onharu.rabbitmq.enabled` (`application-rabbitmq.yaml` 기준 기본 **true**, compose/환경 기본은 **false**) — `false`이면 `RabbitAutoConfiguration`이 제외되고 `config.RabbitMqConfig` / `infra.rabbitmq`가 로드되지 않습니다.
- `onharu.stomp.relay.enabled` — `true`이면 WebSocket이 인메모리 SimpleBroker 대신 RabbitMQ STOMP 브로커 릴레이(포트 61613)를 씁니다. 이 값을 바꾸려면 STOMP 플러그인이 켜진 RabbitMQ가 **반드시** 떠 있어야 합니다.

채팅 이벤트는 `onharu.kafka.outbox.enabled`로 두 경로가 갈립니다.

1. `false` → STOMP 핸들러가 `KafkaProducer`로 직접 발행합니다.
2. `true` → 이벤트가 `outbox_events`에 들어가고(`domain/outbox/`, `infra/kafka/outbox/` 참고) 스케줄러가 Kafka로 릴레이합니다. DB 쓰기와 원자성이 필요할 때 사용합니다.

### 보안 / 인증

- **세션 기반 인증만** 사용합니다. `infra/security/jwt`는 비어 있으며 JWT 토큰 플로우는 없습니다. REST는 Spring Security 세션 쿠키로 사용자를 식별합니다(`SecurityConfig`, `SessionConfig`, `LocalUser`/`SocialUser` 참고).
- `SecurityConfig`는 `@Profile("!test")` — 테스트 프로필은 별도 체인을 씁니다.
- 대부분의 `/api/**` 경로는 `PUBLIC_PATH`에 있고, 역할 강제는 선택적으로 적용됩니다(`ROLE_CHILD_PATH`, `ROLE_OWNER_PATH`, `ROLE_ADMIN_PATH`). 보호가 필요한 엔드포인트를 추가할 때는 기본값에만 의존하지 말고 이 배열들을 갱신하세요.

### 영속성 + 캐시

- prod/dev는 MySQL 8, 테스트는 H2(`MODE=MYSQL`). dev에서는 `ddl-auto=update` — 스키마 드리프트는 조용히 허용됩니다. prod/CI에서는 `JPA_DDL_AUTO=validate`로 덮어쓰세요.
- P6Spy는 런타임 클래스패스에 포함되어 있으며, SQL 로깅은 `application-jpa-logging.yaml` 프로필 include로 제어합니다.
- dev에서 앱 기동 시 Redis는 **필수**입니다(최근 검색 Redis 저장소가 컨텍스트 초기화 시 연결됨). Redisson은 `infra/redis/lock/DistributeLockExecutor`로 분산 락을 제공합니다.
- Spring Cache는 Redis를 백엔드로 쓰며, Jackson + `jackson-datatype-hibernate6`로 JPA 프록시를 처리합니다.

### 뷰 / 템플릿

- Thymeleaf MVC 뷰는 **비활성**입니다(`spring.thymeleaf.enabled: false`). Thymeleaf는 `ThymeleafMailConfig`를 통해서만 `resources/templates/mail/` 메일 HTML에 연결됩니다. Thymeleaf 렌더링을 기대하는 뷰 컨트롤러를 추가하지 마세요.
- Swagger UI는 `/swagger-ui/index.html`, OpenAPI JSON은 `/api-docs/json` — `swagger` 프로필이 포함될 때만(`application.yaml` 기본 포함).

## 프로필

`application.yaml`에서 `spring.profiles.active=dev, include=swagger`를 설정합니다. 이어서 `application-dev.yaml`이 `smtp`, `oauth`, `nts`, `jpa-logging`(필수)과 `kafka`, `rabbitmq`(선택)를 import합니다. 새 프로필 YAML을 추가할 때, 대응하는 백엔드 서비스가 `enabled` 플래그로 막혀 있으면 `optional:` 접두 규칙을 따르세요.

## 추가 문서

도메인 수준 참고 자료는 `docs/`에 있습니다 — `PROJECT_STRUCTURE.md`, `DOMAIN_ARCHITECTURE.md`, `ERD.md`, `API_SPEC.md`, `SEQUENCE_DIAGRAM.md`, `STOMP.md`, `RABBITMQ.md`, `TROUBLE_SHOOTING.md`.
