## 문서 목차 (`docs/`)

### 개요·구조

| 문서 | 설명 |
|------|------|
| [PROJECT_STRUCTURE.md](docs/PROJECT_STRUCTURE.md) | 기술 스택, Gradle, 패키지 구조, 레이어 설명 |
| [REQUIREMENT.md](docs/REQUIREMENT.md) | 기능·비기능 요구사항(결식 아동·가게·예약·관리자 등) |

### API·명세

| 문서 | 설명 |
|------|------|
| [API_SPEC.md](docs/API_SPEC.md) | REST 공통 응답·오류 규약, 컨트롤러별 HTTP 엔드포인트, WebSocket 요약 |

### 도메인·데이터

| 문서 | 설명 |
|------|------|
| [DOMAIN_ARCHITECTURE.md](docs/DOMAIN_ARCHITECTURE.md) | 도메인 개념, 레이어, 엔티티 관계, 이벤트·Redis 등 횡단 관심 |
| [ERD.md](docs/ERD.md) | 테이블·FK 관계(JPA 엔티티 기준), 제약 요약 |

### 다이어그램

| 문서 | 설명 |
|------|------|
| [CLASS_DIAGRAM.md](docs/CLASS_DIAGRAM.md) | Facade·도메인 서비스·리포지토리·핵심 엔티티 의존 관계 |
| [SEQUENCE_DIAGRAM.md](docs/SEQUENCE_DIAGRAM.md) | 로그인·예약·예약 확정·STOMP 채팅 등 호출 순서 |

### 노트·트러블슈팅·학습

| 문서 | 설명 |
|------|------|
| [ANTI_PATTERN.md](docs/ANTI_PATTERN.md) | 피하고 싶은 패턴 메모(예: 서블릿 객체를 서비스로 전달 등) |
| [TROUBLE_SHOOTING.md](docs/TROUBLE_SHOOTING.md) | 분산 락·동시성 테스트 이슈 등 트러블슈팅 기록 |
| [KAFKA_LEARN_LOG.md](docs/KAFKA_LEARN_LOG.md) | Kafka 개념 학습 정리(본 서비스는 `build.gradle` 기준 Spring Kafka 미사용) |