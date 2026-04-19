# 프로젝트 구조 (onharu-backend-v2)

## 기술 스택

### 주요 기술

- **Java 17** (`build.gradle` `java.toolchain`)
- **Spring Boot 3.3.4**
- **Spring Web**, **Spring Data JPA**, **Validation**, **AOP**
- **Spring Security** + **OAuth2 Client** (소셜 로그인 등)
- **Spring Security 세션** (`SessionConfig`, `HttpSessionEventPublisher` 등) — 코드베이스 기준 **JWT 토큰 기반 인증은 사용하지 않음** (`infra/security/jwt` 패키지는 비어 있음)
- **Spring Mail** (SMTP)
- **Spring WebSocket** + **STOMP** (`@EnableWebSocketMessageBroker`, `infra.websocket`)
- **Spring Cache** + **Spring Data Redis**
- **Redisson** (`redisson-spring-boot-starter` 3.16.4, 분산 락 등)
- **Thymeleaf** 스타터는 포함되나 **`spring.thymeleaf.enabled: false`** 로 MVC 뷰는 끄고, **`ThymeleafMailConfig`** 로 **메일 HTML 템플릿**(`templates/mail/`)만 사용
- **Springdoc OpenAPI** (`springdoc-openapi-starter-webmvc-ui` **2.1.0**) — `application-swagger.yaml` 에서 `/api-docs/json`, Swagger UI 경로 등 설정

### 빌드·품질

- **Gradle Groovy DSL** (`build.gradle`, `settings.gradle`)
- **JaCoCo** 0.8.11, 테스트 실행 후 `jacocoTestReport` 연동
- **SonarQube** Gradle 플러그인 4.4.1.3373, `sonar.coverage.jacoco.xmlReportPaths` 로 JaCoCo XML 연동

### 데이터베이스·관측

- **MySQL 8** (`mysql-connector-j`, 런타임)
- **테스트**: **H2** (`testRuntimeOnly`), `test` 태스크에 `spring.profiles.active=test` 고정
- **SQL 추적**: **P6Spy** (`p6spy`, 런타임) — 프로파일로 선택 적용 (`application-jpa-logging.yaml` 등)

### 외부 연동·기타 라이브러리

- **AWS SDK v2** (BOM 2.20.26) — S3 (`S3ConfigLocal`, `LocalS3Service` / `StorageService` 등)
- **Apache POI** `poi-ooxml` 5.2.5 — 엑셀 (`StoreExcelFacade`)
- **Commons IO**, **Commons Lang3**, **Commons Collections4**
- **테스트**: **embedded-redis** — `test` 프로필에서 Redis/분산 락 관련 테스트 지원

### Kafka

- **`spring-kafka`** (`build.gradle`) — **선택적** 사용. `onharu.kafka.enabled=false` 이면 `KafkaAutoConfiguration` 없이 기동하고, `true` 일 때 **`config.KafkaConfig`** 로 `KafkaTemplate`·ConsumerFactory·리스너 팩토리를 등록하고, **`infra.kafka`** 에 Producer/Consumer 빈·**트랜잭션 아웃박스**(`infra.kafka.outbox`, 릴레이 스케줄은 `outbox/scheduler`)가 로드됩니다.
- 예약 등 **도메인 내부 이벤트**는 기존처럼 `ApplicationEventPublisher` + `event` 패키지 리스너를 사용합니다(Kafka와 별개).

### RabbitMQ

- **`spring-boot-starter-amqp`** — **선택적** 사용. `onharu.rabbitmq.enabled=false`(기본)이면 `RabbitAutoConfiguration` 없이 기동하고, `true` 일 때 `config.RabbitMqConfig`·`infra.rabbitmq`(채팅 큐 발행/구독)가 로드됩니다. 상세는 `docs/RABBITMQ.md`, Docker는 `docker-compose.yml` 의 `rabbitmq` 서비스.

---

## 애플리케이션 형태

- **REST API + WebSocket(STOMP)**. JSP 등 서버 사이드 렌더링 뷰는 없음.
- REST 베이스 경로는 컨트롤러마다 **`/api/...`** (예: `/api/stores`, `/api/auth`, `/api/chats`).
- **예약(Reservation)** 은 별도 `/api/reservations` 단일 리소스가 아니라 **`ChildFacade` / `OwnerFacade` / `StoreFacade` 흐름**과 **`/api/childrens`**, **`/api/owners`**, **`/api/stores`** 등에 노출됨.
- **정적 리소스**: `src/main/resources/static/` (`index.html`, `sql/create-database-schema.sql` 등).
- **메일 템플릿**: `src/main/resources/templates/mail/` (Thymeleaf HTML).
- **API 문서**: 기본 `application.yaml` 의 `spring.profiles.include` 에 **`swagger`** 포함 시 `application-swagger.yaml` 설정 적용.

### WebSocket (STOMP) 요약

- 연결 엔드포인트: **`/ws-chat`** (`WebSocketConfiguration`)
- 브로커 prefix: **`/topic`**, **`/queue`** / 앱 prefix: **`/app`**
- 메시지 처리: `infra.websocket.ChatMessageStompHandler` — `@MessageMapping("/chat/send")` 등

---

## 패키지 구조 (요약)

루트 패키지: **`com.backend.onharu`**

```
com.backend.onharu/
├── OnharuApplication.java
├── config/                         # 스프링 설정 (보안, Redis, 캐시, WebSocket, 메일, S3, 스케줄, 비동기 등)
│   ├── SecurityConfig.java
│   ├── SessionConfig.java
│   ├── RedisTemplateConfig.java, RedisCacheConfig.java, RedissonConfiguration.java
│   ├── WebSocketConfiguration.java
│   ├── MailConfig.java, ThymeleafMailConfig.java
│   ├── SwaggerConfig.java
│   ├── S3ConfigLocal.java, RestTemplateConfig.java
│   ├── PasswordEncoderConfig.java, ServerUrlProperties.java
│   ├── ScheduleConfig.java, AsyncConfig.java
│   ├── KafkaConfig.java              # onharu.kafka.enabled=true 일 때만 (Producer/Consumer 팩토리, KafkaTemplate)
│   └── …
├── application/                    # 유스케이스 조립
│   ├── *Facade.java                # 12개: Auth, Chat, Child, File, Level, Notification, Owner, Review, Store, StoreExcel, StoreSchedule, User
│   ├── StoreRecentSearchService.java
│   ├── validator/StoreScheduleValidator.java
│   └── dto/                        # 퍼사드 전용 DTO 등
├── domain/                         # 도메인별 모델·서비스·리포지토리 인터페이스·DTO
│   ├── outbox/                     # 트랜잭션 아웃박스(OutboxEvent, OutboxEventRepository …)
│   ├── event/                      # ChatKafkaOutboxPort, ChatRabbitPublishPort 등 메시징 포트
│   ├── chat/
│   ├── child/
│   ├── common/                     # BaseEntity, Enum, JpaAuditingConfig, SecurityAuditorAware 등
│   ├── email/
│   ├── favorite/
│   ├── file/
│   ├── level/
│   ├── notification/
│   ├── owner/
│   ├── reservation/
│   ├── review/
│   ├── store/
│   ├── storeschedule/
│   ├── tag/
│   ├── upload/                     # S3/스토리지 추상 (StorageService, LocalS3Service …)
│   ├── user/
│   └── support/                    # CacheName, error(CoreException, ErrorCode, ErrorType …)
├── infra/                          # 인프라 구현
│   ├── db/                         # JPA, *JpaRepository, *RepositoryImpl (도메인별 하위 패키지, `db/outbox` 등)
│   ├── kafka/                      # KafkaTemplate, Consumer, 아웃박스(`outbox/`, 릴레이 스케줄 `outbox/scheduler/`) — 설정은 config.KafkaConfig
│   ├── rabbitmq/                   # RabbitTemplate, 채팅 이벤트 큐 발행·리스너 (연결·큐 빈은 config.RabbitMqConfig)
│   ├── redis/                      # 캐시(해시), 조회수·카운트, 최근 검색어, 분산 락(DistributeLockExecutor) 등
│   ├── security/                   # LocalUser·LocalUserService, OAuth2 핸들러, SocialUser — `jwt` 하위 패키지는 비어 있음
│   │   └── oauth/                  # OAuth2Success/FailureHandler, SocialUserService, OAuthAttributes …
│   ├── email/                      # EmailSender, EmailSendService, impl
│   ├── nts/                        # 사업자번호 등 외부 연동 (NtsBusinessNumber, NtsBusinessNumberImpl)
│   └── websocket/                  # STOMP DTO·ChatMessageStompHandler
├── interfaces/
│   ├── api/
│   │   ├── controller/             # I*Controller + impl/*ControllerImpl
│   │   ├── dto/
│   │   ├── common/                 # ErrorResponse, PageableUtil 등
│   │   └── support/                # ApiControllerAdvice, ClientIdentityResolver
│   └── shceduler/                  # @Scheduled (패키지명 철자: shceduler)
│       ├── ReservationScheduler.java
│       └── StoreViewCountScheduler.java
├── event/                          # ReservationEvent, ReservationNotificationMessage, listener (도메인 `event` 패키지의 ChatKafkaOutboxPort 등과 별개)
└── utils/                          # DateUtils, NumberUtils, CookieUtils, SecurityUtils
```

### REST 컨트롤러와 매핑 베이스 (구현체 `@RequestMapping` 기준)

| 베이스 경로 | 구현 클래스 |
|------------|-------------|
| `/api/auth` | `AuthControllerImpl` |
| `/api/users` | `UserControllerImpl` |
| `/api/childrens` | `ChildrenControllerImpl` |
| `/api/owners` | `OwnerControllerImpl` |
| `/api/admins` | `AdminControllerImpl` |
| `/api/stores` | `StoreControllerImpl`, `StoreRecentSearchControllerImpl` |
| `/api/reviews` | `ReviewControllerImpl` |
| `/api/favorites` | `FavoriteControllerImpl` |
| `/api/notifications` | `NotificationControllerImpl` |
| `/api/chats` | `ChatControllerImpl` |
| `/api/levels` | `LevelControllerImpl` |
| `/api/upload` | `S3ControllerImpl` |

### 레이어링에 대한 짧은 설명

- **`domain`**: 비즈니스 규칙과 포트(리포지토리 인터페이스). 다른 레이어에 덜 의존하도록 유지하는 것이 목표.
- **`infra`**: JPA·Redis·보안·외부 HTTP·WebSocket 등 기술 세부 구현. `domain` 의 인터페이스를 구현.
- **`application`**: `*Facade` 로 트랜잭션 경계·도메인 서비스 조합. `StoreExcelFacade` 는 POI 기반 엑셀, `StoreRecentSearchService` 는 검색어 등 부가 유스케이스.
- **`interfaces.api`**: HTTP 요청/응답 매핑. `ApiControllerAdvice` 로 REST 전역 예외 처리.

### 설정 프로파일 파일 (참고)

`src/main/resources/` — `application.yaml` 외 `application-dev.yaml`, `application-prod.yaml`, `application-test.yaml`, `application-oauth.yaml`, `application-smtp.yaml`, `application-nts.yaml`, `application-swagger.yaml`, `application-jpa-logging.yaml`, **`application-kafka.yaml`**(Kafka·아웃박스, 선택 import) 등 프로파일별 분리.
