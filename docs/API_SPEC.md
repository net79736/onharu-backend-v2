# API 명세 (onharu-backend-v2)

본 문서는 **`interfaces.api.controller.impl.*ControllerImpl`** 에 정의된 **HTTP 매핑**과, 공통 응답·전역 예외 처리(`ResponseDTO`, `ApiControllerAdvice`)를 근거로 정리한다.  
이전에 다른 프로젝트(영화관 예매·대기열·지갑 등)용으로 작성된 내용은 **본 백엔드와 무관**하므로 사용하지 않는다.

---

## 1. API 탐색·문서

| 항목 | 값 |
|------|-----|
| 기본 서버 URL (로컬 예시) | `http://localhost:8080` (`application-swagger.yaml` 의 `springdoc.server.url`) |
| OpenAPI JSON | `GET /api-docs/json` |
| Swagger UI | `http://localhost:8080/swagger-ui/index.html` (`swagger` 프로파일 포함 시) |
| 스캔 패키지 | `com.backend.onharu` |

요청/응답 **필드 스키마·예시**는 **Swagger UI**가 최신이다.

---

## 2. 공통 규약

### 2.1 헤더·본문

| 항목 | 설명 |
|------|------|
| `Content-Type` | JSON 본문이 있을 때 `application/json` (기본값은 `application/json;charset=UTF-8` 설정) |
| `multipart` | 가게 엑셀 업로드 등 `multipart/form-data` (`StoreControllerImpl` `/upload-excel`) |

**대기열 토큰·`X-User-Id` 헤더 기반 인증은 본 프로젝트 REST에 존재하지 않는다.** 사용자 식별은 **Spring Security 세션**(로그인 후 쿠키) 등 구현을 따른다.

### 2.2 성공 응답 (`ResponseDTO`)

```text
com.backend.onharu.interfaces.api.common.dto.ResponseDTO<T>
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `boolean` | 성공 시 `true` |
| `data` | `T` | 업무 데이터 |

정적 팩토리: `ResponseDTO.success(data)`.

### 2.3 오류 응답 (`ErrorResponse`)

```text
com.backend.onharu.interfaces.api.common.dto.ErrorResponse
```

| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `Boolean` | 실패 시 `false` |
| `code` | `String` | 예: `BAD_REQUEST`, `UNAUTHORIZED`, `NOT_FOUND` … |
| `message` | `String` | 사용자 메시지 |

**처리기** (`ApiControllerAdvice`):

- `CoreException`: `ErrorType` 에 따라 **400 / 401 / 403 / 404 / 409 / 500** 등 매핑.
- `MethodArgumentNotValidException` (Bean Validation): **400**, 메시지는 첫 필드 오류 등.
- 그 외 예외: **500**, 고정 메시지.

---

## 3. REST 엔드포인트 목록

아래는 **클래스 레벨 `@RequestMapping` + 메서드 매핑**을 합친 **전체 경로**이다.

### 3.1 Admin — `AdminControllerImpl` 베이스 `/api/admins`

| Method | Path |
|--------|------|
| `POST` | `/api/admins/owners/{requestId}/approve` |
| `POST` | `/api/admins/owners/{requestId}/reject` |
| `POST` | `/api/admins/children/{requestId}/approve` |
| `POST` | `/api/admins/children/{requestId}/reject` |

### 3.2 Auth — `AuthControllerImpl` 베이스 `/api/auth`

| Method | Path |
|--------|------|
| `POST` | `/api/auth/business-number` |
| `POST` | `/api/auth/find-id` |
| `POST` | `/api/auth/reset-password` |
| `POST` | `/api/auth/email/send-code` |
| `POST` | `/api/auth/email/verify-code` |
| `POST` | `/api/auth/change-password` |
| `POST` | `/api/auth/validate-password` |

※ SMS 관련 매핑은 소스에서 **주석 처리**됨.

### 3.3 User — `UserControllerImpl` 베이스 `/api/users`

| Method | Path |
|--------|------|
| `POST` | `/api/users/signup/owner` |
| `POST` | `/api/users/signup/child` |
| `GET` | `/api/users/profile/child` |
| `GET` | `/api/users/profile/owner` |
| `PUT` | `/api/users/profile/child` |
| `PUT` | `/api/users/profile/owner` |
| `DELETE` | `/api/users` |
| `POST` | `/api/users/login` |
| `POST` | `/api/users/logout` |
| `POST` | `/api/users/signup/child/finish` |
| `POST` | `/api/users/signup/owner/finish` |
| `GET` | `/api/users/me` |
| `GET` | `/api/users/search` |

### 3.4 Children — `ChildrenControllerImpl` 베이스 `/api/childrens`

| Method | Path |
|--------|------|
| `POST` | `/api/childrens/cards` |
| `PUT` | `/api/childrens/cards/{cardId}` |
| `DELETE` | `/api/childrens/cards/{cardId}` |
| `POST` | `/api/childrens/cards/{cardId}/reissue` |
| `GET` | `/api/childrens/cards/{cardId}` |
| `POST` | `/api/childrens/certificate` |
| `PUT` | `/api/childrens/certificate/{certificateId}` |
| `DELETE` | `/api/childrens/certificate/{certificateId}` |
| `GET` | `/api/childrens/certificate/{certificateId}` |
| `POST` | `/api/childrens/stores/{storeId}/reservations` |
| `POST` | `/api/childrens/reservations/{reservationId}/cancel` |
| `GET` | `/api/childrens/reservations` |
| `GET` | `/api/childrens/reservations/{reservationId}` |
| `GET` | `/api/childrens/reservations/summary` |

### 3.5 Owner — `OwnerControllerImpl` 베이스 `/api/owners`

| Method | Path |
|--------|------|
| `POST` | `/api/owners/business` |
| `PUT` | `/api/owners/{ownerId}/business` |
| `DELETE` | `/api/owners/business/{ownerId}` |
| `GET` | `/api/owners/business/{ownerId}` |
| `POST` | `/api/owners/stores` |
| `PUT` | `/api/owners/stores/{storeId}` |
| `DELETE` | `/api/owners/stores/{storeId}` |
| `GET` | `/api/owners/stores` |
| `GET` | `/api/owners/stores/{storeId}/reservations` |
| `GET` | `/api/owners/stores/{storeId}/reservations/{reservationId}` |
| `POST` | `/api/owners/reservations/{reservationId}/approve` |
| `POST` | `/api/owners/reservations/{reservationId}/complete` |
| `POST` | `/api/owners/reservations/{reservationId}/cancel` |
| `POST` | `/api/owners/stores/{storeId}/schedules` |
| `PUT` | `/api/owners/stores/{storeId}/schedules` |
| `DELETE` | `/api/owners/stores/{storeId}/schedules` |
| `GET` | `/api/owners/reservations/summary` |

### 3.6 Store — `StoreControllerImpl` 베이스 `/api/stores`

| Method | Path | 비고 |
|--------|------|------|
| `GET` | `/api/stores/{storeId}` | 가게 상세 |
| `GET` | `/api/stores` | 가게 검색·목록 |
| `GET` | `/api/stores/{storeId}/schedules` | 스케줄 조회 |
| `GET` | `/api/stores/categories` | 카테고리 목록 |
| `POST` | `/api/stores/upload-excel` | `multipart/form-data`, part name `file` |

### 3.7 Store recent search — `StoreRecentSearchControllerImpl` 베이스 `/api/stores`

| Method | Path |
|--------|------|
| `GET` | `/api/stores/recent-keywords` |
| `POST` | `/api/stores/recent-keywords` |

### 3.8 Review — `ReviewControllerImpl` 베이스 `/api/reviews`

| Method | Path |
|--------|------|
| `POST` | `/api/reviews/stores/{storeId}` |
| `GET` | `/api/reviews` |
| `GET` | `/api/reviews/stores/{storeId}` |
| `GET` | `/api/reviews/my` |
| `DELETE` | `/api/reviews/{reviewId}` |

### 3.9 Favorite — `FavoriteControllerImpl` 베이스 `/api/favorites`

| Method | Path |
|--------|------|
| `POST` | `/api/favorites/stores/{storeId}` |
| `GET` | `/api/favorites` |

### 3.10 Level — `LevelControllerImpl` 베이스 `/api/levels`

| Method | Path |
|--------|------|
| `POST` | `/api/levels` |
| `GET` | `/api/levels/{levelId}` |
| `GET` | `/api/levels` |
| `PUT` | `/api/levels` |
| `DELETE` | `/api/levels/{levelId}` |

### 3.11 Notification — `NotificationControllerImpl` 베이스 `/api/notifications`

| Method | Path |
|--------|------|
| `GET` | `/api/notifications/me` |
| `PUT` | `/api/notifications/me` |
| `GET` | `/api/notifications/histories` |
| `PUT` | `/api/notifications/histories/{historyId}/read` |
| `PUT` | `/api/notifications/histories/read/all` |

### 3.12 Chat (REST) — `ChatControllerImpl` 베이스 `/api/chats`

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

### 3.13 Upload (S3) — `S3ControllerImpl` 베이스 `/api/upload`

| Method | Path |
|--------|------|
| `GET` | `/api/upload` |
| `DELETE` | `/api/upload/delete` |
| `GET` | `/api/upload/download` |

---

## 4. WebSocket (STOMP) — REST와 별도

REST가 아니라 **메시징**으로 동작한다. 설정은 `WebSocketConfiguration` 참고.

| 항목 | 값 |
|------|-----|
| SockJS/STOMP 엔드포인트 | `/ws-chat` |
| 클라이언트→서버 prefix | `/app` |
| 구독 prefix | `/topic`, `/queue` |
| 메시지 전송 예시 | `@MessageMapping("/chat/send")` → `infra.websocket.ChatController` |

브라우저·클라이언트는 STOMP 클라이언트로 연결 후 `/app/chat/send` 등으로 송신한다.

---

## 5. 인증·노출 범위

엔드포인트별 **인증 필요 여부**는 `SecurityConfig` 의 `PUBLIC_PATH`, `AUTHENTICATE_PATH`, 역할별 경로와 맞춰야 하며, **변경 시 코드가 우선**이다. 본 문서는 **경로 목록(What)** 만 제공하고, **누가 호출 가능한지(Who)** 는 Swagger·보안 설정을 함께 본다.

---

## 6. 관련 문서

- 기능 요약: `docs/REQUIREMENT.md`
- 패키지·기술 스택: `docs/PROJECT_STRUCTURE.md`
