# k6 예약 부하 테스트 요구사항 (HTTP 동기 vs RabbitMQ 비동기 비교)

## 1. 목적

| 방식 | 설명 |
|------|------|
| HTTP 동기 | 예약 요청 시 서버가 즉시 DB에 저장하고 응답 (본 저장소: `POST /api/childrens/stores/{storeId}/reservations` → **201 Created**) |
| RabbitMQ 비동기 | 큐에 적재 후 **즉시 응답(예: 202 Accepted)**, 컨슈머가 비즈니스 처리 (**현재 백엔드에 전용 API 없음** → 별도 구현 또는 `K6_ASYNC_RESERVE_URL`로 연결) |

**목표 지표:** 처리량(iterations/s), `http_req_duration`, 비동기 시 수락 지연 및(선택) 처리 완료까지 대기 시간.

## 2. 라이프사이클

1. **Setup**
   - 테스트 계정 로그인 후 세션 쿠키 확보 (**JWT 아님**: Spring Security 세션, `Cookie` 헤더).
   - 사업자 계정으로 가게 스케줄 일괄 생성 (VU×반복 수만큼 겹치지 않는 슬롯).
     - 날짜 충돌 방지: `K6_SCHEDULE_DAY_OFFSET`(내일 + N일)로 매 실행마다 시작 날짜를 이동 가능
       - `script-lite.js`는 `K6_SCHEDULE_DAY_OFFSET` 미지정 시에도 **자동 날짜 오프셋(1~60일 분산)** 으로 중복 확률을 낮춤
     - 월 고정(권장): `K6_SCHEDULE_YEAR`, `K6_SCHEDULE_MONTH`로 5월/6월 등 특정 월 1일부터 생성 (DAY_OFFSET보다 우선)
     - 기존 데이터와 충돌 시: `K6_SCHEDULE_PREDELETE=1`로 해당 날짜/시간대 겹치는 스케줄 선삭제 후 생성(개발 DB에서만 권장)
   - 공개 API `GET /api/stores/{storeId}/schedules`로 일정 ID 목록 수집.
2. **Running**
   - 결식 아동 계정으로 예약 생성 API 부하 (`K6_RESERVE_MODE=http|async`).
3. **Teardown**
   - 아동 예약 목록 조회 후 취소 가능한 예약 일괄 취소.
   - 사업자로 생성했던 스케줄 ID 일괄 삭제.

## 3. 환경·도구

- **k6** (JavaScript), 백엔드: Spring Boot 3.
- **인증**: `POST /api/users/login` + `Set-Cookie` → 이후 요청에 `Cookie` 헤더 (기존 `load-test/onharu-domains-load.js`와 동일 패턴).
- **RabbitMQ**: 비동기 예약 엔드포인트는 저장소별 구현 필요. 스크립트는 URL만 주입하면 동작하도록 분리.

## 4. 저장소 매핑 (onharu-backend-v2)

| 항목 | 메서드·경로 |
|------|-------------|
| 로그인 | `POST /api/users/login` (`loginId`, `password`) |
| 스케줄 생성 | `POST /api/owners/stores/{storeId}/schedules` (OWNER 역할) |
| 스케줄 삭제 | `DELETE /api/owners/stores/{storeId}/schedules` (`storeScheduleIds`) |
| 일정 ID 조회 | `GET /api/stores/{storeId}/schedules?year=&month=&day=` → `data.scheduleSlots[].id` |
| 예약 생성(동기) | `POST /api/childrens/stores/{storeId}/reservations` (`storeScheduleId`, `people`) |
| 내 예약 목록 | `GET /api/childrens/reservations` (페이징) |
| 예약 취소 | `POST /api/childrens/reservations/{reservationId}/cancel` |

## 5. 스크립트 구성

| 파일 | 역할 |
|------|------|
| `config.js` | `BASE_URL`, VU, duration, 모드, 비동기 URL 등 |
| `api.js` | 로그인·스케줄·예약·목록·취소 HTTP 래퍼 |
| `script.js` | `setup` / `default` / `teardown`, 커스텀 메트릭 |
| `api-smoke.js` | `api.js`의 각 함수가 정상 응답을 주는지 1회씩 스모크 검증 |
| `script-lite.js` | `script.js`의 “필수 흐름만” 남긴 짧은 로드 테스트 버전 |

## 6. Cursor 프롬프트 예시

```text
@load-test/reservation-compare/test-requirements.md 와 api.js를 읽고,
k6로 HTTP 예약과 K6_ASYNC_RESERVE_URL 비동기 예약을 비교할 수 있게 script.js를 유지보수해줘.
setup에서 로그인·스케줄 생성, teardown에서 예약 취소·스케줄 삭제가 빠지지 않게 해줘.
```

## 7. 실행 예시

```bash
cd load-test
export K6_OWNER_LOGIN_ID='owner123@test.com'
export K6_OWNER_PASSWORD='Password123!'
export K6_CHILD_LOGIN_ID='child123@test.com'
export K6_CHILD_PASSWORD='Password123!'
export K6_STORE_ID='1'

# api.js 함수별 스모크 (정상 동작 여부만 빠르게 확인)
k6 run reservation-compare/api-smoke.js

# (추천) 짧은 로드 테스트 버전 (필수 흐름만)
K6_RESERVE_MODE=http k6 run reservation-compare/script-lite.js

# (선택) VU/반복/최대시간/슬립 (config.js 공통)
# ONHARU_VUS=8 ONHARU_ITERATIONS=20 ONHARU_MAX_DURATION=30m ONHARU_SLEEP_SEC=0 ...

# (선택) 비동기(async) — 비동기 엔드포인트가 있을 때
K6_RESERVE_MODE=async \
K6_ASYNC_RESERVE_URL='http://localhost:8080/api/...' \
K6_ASYNC_POLL_URL_TEMPLATE='http://localhost:8080/api/.../{id}' \
k6 run reservation-compare/script-lite.js

# 동기만
K6_RESERVE_MODE=http k6 run reservation-compare/script.js

# (권장) 실행마다 날짜를 밀어서 기존 일정과 충돌 방지
K6_SCHEDULE_DAY_OFFSET=30 K6_RESERVE_MODE=http k6 run reservation-compare/script.js

# (권장) 5월/6월로 월 고정 (4월 데이터가 많을 때)
K6_SCHEDULE_YEAR=2026 K6_SCHEDULE_MONTH=5 K6_RESERVE_MODE=http k6 run reservation-compare/script.js
K6_SCHEDULE_YEAR=2026 K6_SCHEDULE_MONTH=6 K6_RESERVE_MODE=http k6 run reservation-compare/script.js

# (주의) 충돌 시 선삭제까지 수행 (테스트용 storeId/개발 DB에서만)
K6_SCHEDULE_PREDELETE=1 K6_RESERVE_MODE=http k6 run reservation-compare/script.js

# 비동기 엔드포인트가 있을 때 (202 등)
K6_RESERVE_MODE=async K6_ASYNC_RESERVE_URL='http://localhost:8080/api/...' k6 run reservation-compare/script.js
```
