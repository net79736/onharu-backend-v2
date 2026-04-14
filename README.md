1. HttpServletRequest, HttpServletReponse 는 서비스 레이어로 내려가는건 안좋은 패턴이다.
별도로 따로 *Resolver 클래스를 만들어서 거기서 관리하는게 좋다.

---

# [트러블슈팅] 분산 락(Distributed Lock)이 있는데 왜 동시성 테스트가 실패할까?

> 분산 락을 걸었는데도 테스트에서 중복 예약이 발생했던 이슈와 해결 과정을 기록합니다.

---

## 상황 요약

온하루(OnHaru) 프로젝트에서 **가게 일정 예약 기능**을 구현하면서 동시성 문제를 막기 위해 Redis 분산 락(Redisson)을 적용했습니다.

그런데 이상한 일이 발생했습니다.

- **UI에서 직접 테스트**: 동시에 여러 번 버튼을 눌러도 예약은 1건만 생성됨 ✅
- **테스트 코드(CompletableFuture 10개 동시 실행)**: 예약이 1건이 아닌 여러 건 생성됨 ❌

분산 락은 분명히 걸려 있는데, 왜 테스트에서만 실패할까요?

---

## 기존 코드 구조

```java
// ChildFacade.java
// @Transactional  ← 주석 처리되어 있었지만, 하위 Service에는 @Transactional이 걸려 있음
public void reserve(CreateReservationCommand command) {

    // ...유효성 검증...

    String lockName = "lock:reservation:storeSchedule:" + command.storeScheduleId();
    distributeLockExecutor.execute(() -> {
        // 이미 예약 있는지 확인
        Reservation reservation = reservationQueryService.getByStoreScheduleId(...);
        if (reservation != null && !reservation.isAvailable()) {
            throw new CoreException(RESERVATION_ALREADY_EXISTS);
        }

        // 예약 생성 (내부에 @Transactional이 걸린 Service 호출)
        reservationCommandService.createReservation(command, storeSchedule, child);

        return null;
    }, lockName, 10_000, 10_000);
}
```

겉보기에는 문제없어 보입니다. 락 안에서 조회하고, 없으면 생성합니다.

---

## 원인 분석

### 핵심 문제: 락 해제 시점 vs 트랜잭션 커밋 시점

`reservationCommandService.createReservation()` 안에는 `@Transactional`이 걸려 있습니다. Spring의 트랜잭션은 **해당 메서드가 완전히 끝나고 프록시가 반환될 때 커밋**됩니다.

그런데 `distributeLockExecutor.execute()` 는 **supplier 람다가 끝나면 바로 락을 해제**합니다.

```
스레드 A: [락 획득] → [예약 없음 확인] → [createReservation 호출] → [람다 종료] → [락 해제] → [트랜잭션 커밋]
                                                                                         ↑
스레드 B:                                                                          [락 획득] → [예약 조회 → 아직 커밋 안됨 → 없다고 판단] → [예약 생성] ❌
```

즉, **스레드 A가 락을 풀었을 때 DB에는 아직 데이터가 없습니다.** 스레드 B가 들어와서 조회하면 빈 테이블을 보고 "예약 없음"으로 판단해 또 예약을 생성하는 것입니다.

### 왜 UI 테스트에서는 성공했나?

UI에서의 HTTP 요청은 **한 요청이 완전히 처리되고 응답을 반환한 뒤**에 다음 요청이 들어옵니다. 즉, 이미 트랜잭션이 커밋된 상태에서 다음 요청이 처리되기 때문에 문제가 없었던 것입니다.

`CompletableFuture` 10개는 **진짜로 동시에** 실행되기 때문에 이 타이밍 문제가 드러납니다.

---

## 해결 방법

### 황금률: 락의 범위는 반드시 트랜잭션의 범위보다 넓어야 한다

```
잘못된 구조: [트랜잭션 시작 [락 획득 → 저장 → 락 해제] ... 커밋]
올바른 구조: [락 획득 [트랜잭션 시작 → 저장 → 커밋] 락 해제]
```

락 안에서 트랜잭션이 **완전히 커밋된 후** 락이 해제되어야 합니다.

### 해결책: 락 내부 로직을 별도 Service로 분리

Spring의 `@Transactional`은 **별도 빈(Bean)에서 호출될 때만 프록시가 동작**합니다. 따라서 람다 안에서 직접 `@Transactional` 메서드를 호출하는 것만으로는 트랜잭션 커밋 순서를 보장할 수 없습니다.

**새로운 Service를 만들어 트랜잭션 경계를 명확히 분리합니다.**

```java
// ReservationTransactionService.java (새로 생성)
@Service
@RequiredArgsConstructor
public class ReservationTransactionService {

    @Transactional  // ← 이 메서드가 끝나면 커밋됨
    public void reserveInTransaction(
            CreateReservationCommand command,
            StoreSchedule storeSchedule,
            Child child,
            Owner owner) {

        if (!storeSchedule.isBookableAt(LocalDateTime.now())) {
            throw new CoreException(RESERVATION_SCHEDULE_TIME_EXPIRED);
        }

        Reservation reservation = reservationQueryService.getByStoreScheduleId(
            new GetByStoreScheduleIdQuery(command.storeScheduleId())
        );
        if (reservation != null && !reservation.isAvailable()) {
            throw new CoreException(RESERVATION_ALREADY_EXISTS);
        }

        Reservation saved = reservationCommandService.createReservation(command, storeSchedule, child);
        applicationEventPublisher.publishEvent(new ReservationEvent(...));
    }
}
```

```java
// ChildFacade.java (변경 후)
public void reserve(CreateReservationCommand command) {
    Child child = childQueryService.getChildById(...);
    StoreSchedule storeSchedule = storeScheduleQueryService.getStoreScheduleById(...);

    // ...유효성 검증...

    String lockName = "lock:reservation:storeSchedule:" + command.storeScheduleId();
    distributeLockExecutor.execute(() -> {
        // 별도 Service 호출 → 람다 안에서 트랜잭션이 시작되고 커밋까지 완료됨
        reservationTransactionService.reserveInTransaction(command, storeSchedule, child, owner);
        return null;
    }, lockName, 10_000, 10_000);
    // ↑ 여기서 락 해제 → 이미 커밋 완료된 상태 ✅
}
```

실행 순서가 이렇게 바뀝니다.

```
스레드 A: [락 획득] → [트랜잭션 시작] → [예약 없음 확인] → [예약 생성] → [트랜잭션 커밋] → [락 해제]
                                                                                                 ↑
스레드 B:                                                                                  [락 획득] → [예약 있음 확인] → [예외 발생] ✅
```

---

## 역할 분리 정리


| 클래스                             | 역할                    | 트랜잭션             |
| ------------------------------- | --------------------- | ---------------- |
| `ChildFacade`                   | 전체 흐름 제어, 분산 락 관리     | 없음               |
| `ReservationTransactionService` | 락 보호 아래 실행되는 순수 DB 로직 | `@Transactional` |


---

## 결론

> **"데이터를 확실히 저장(Commit)한 후에만 락(Lock)을 풀어야 한다"**

분산 락을 아무리 잘 걸어도, 트랜잭션 커밋이 락 해제보다 늦으면 의미가 없습니다. 락은 항상 트랜잭션을 완전히 감싸야 합니다.

## UI 테스트에서는 문제가 없었기 때문에 배포 전에 발견하기 어려운 유형의 버그였습니다. 테스트 코드로 진짜 동시성 상황을 만들어 검증하는 것이 얼마나 중요한지 다시 한번 체감한 경험이었습니다.

---

# [Troubleshooting] Redis 캐싱 시 연관 엔티티(Category) 누락 및 직렬화 오류 해결

`StoreWithFavoriteCount` 객체에 `@Cacheable`을 적용하여 Redis 캐싱을 구현하는 과정에서 발생한 두 가지 주요 문제와 해결 과정을 기록합니다.

---

## 1. 문제 상황 (Problem)

캐싱 적용 후 다음과 같은 두 가지 결함이 발견되었습니다.

* **문제 A. 직렬화 오류:** `LocalDateTime` 및 JPA 엔티티를 Redis에 저장하는 과정에서 Jackson 직렬화 예외가 발생했습니다. 기본 `ObjectMapper`가 Java 8 날짜 API와 Hibernate 프록시 객체를 처리하지 못하는 것이 원인이었습니다.
* **문제 B. Category 데이터 누락:** 캐시 히트(Cache Hit) 시 `store.getCategory()`가 `null`을 반환했습니다. DB에는 정상적으로 저장되어 있음에도 불구하고 화면에 카테고리 정보가 뜨지 않는 현상이 발생했습니다.

---

## 2. 원인 분석 (Cause)

### 원인 A. ObjectMapper 설정 누락
* `JavaTimeModule`이 등록되지 않아 `LocalDateTime` 직렬화에 실패했습니다.
* `Hibernate6Module` 설정 없이는 JPA의 지연 로딩용 프록시 객체를 Jackson이 제대로 해석하지 못해 오류를 유발했습니다.

### 원인 B. 일반 JOIN과 지연 로딩(Lazy Loading)의 특성
* 기존 쿼리 `JOIN s.category c`는 조인 조건으로만 사용되었습니다.
* `SELECT s`를 통해 `Store` 엔티티만 가져올 경우, 연관된 `category`는 여전히 **프록시(가짜 객체)** 상태로 남습니다.
* Jackson 직렬화기는 이 프록시를 실제 데이터가 없는 것으로 판단하여 `null`로 치환한 뒤 캐시에 저장했습니다.

---

## 3. 해결 방안 (Solution)

### ① JPQL: 일반 JOIN을 FETCH JOIN으로 변경
`SELECT` 결과에 `Category`가 실제 객체로 포함되어 로드될 수 있도록 쿼리를 수정했습니다.

```java
// [Before] 조인만 수행하여 category는 프록시 상태로 남음
"FROM Store s JOIN s.category c ..."

// [After] FETCH JOIN을 사용하여 한 번에 실제 데이터를 로드
"FROM Store s JOIN FETCH s.category c ..."
```

### ② ObjectMapper 필수 모듈 3종 추가
Redis 직렬화에 사용되는 `ObjectMapper`에 아래와 같이 필수 설정을 추가했습니다.

```java
// LocalDateTime 등 Java 8 날짜/시간 API 지원
objectMapper.registerModule(new JavaTimeModule());

// JPA(Hibernate) 프록시 객체 정상 처리
objectMapper.registerModule(new Hibernate6Module());

// 날짜를 숫자 배열이 아닌 ISO 8601 문자열 형식으로 저장
objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
```

---

## 4. 결과 및 교훈 (Result & Insight)

### 최종 결과
* **데이터 정합성 확보:** 캐시에서 데이터를 꺼내올 때 `Store` 내부에 `Category` 정보가 정상적으로 포함되어 화면에 노출됩니다.
* **직렬화 안정화:** `LocalDateTime` 및 Hibernate 프록시 객체들이 오류 없이 Redis에 정상적으로 저장 및 조회됩니다.

### 핵심 교훈
> **"캐시는 DB 없이 혼자 살 수 있어야 한다."**

* 캐시 저장소에 데이터를 넣는 순간 해당 객체는 DB와의 연결이 끊긴 상태로 존재해야 합니다.
* 프록시(껍데기) 상태 그대로 캐시에 넣으면, 나중에 꺼냈을 때 데이터를 채워줄 DB 세션이 없어 결국 빈 값만 남게 됩니다.
* **JOIN**은 검색 조건용으로, **JOIN FETCH**는 실제 데이터 로딩용으로 구분하여 사용해야 하며, **캐싱 대상이라면 반드시 FETCH JOIN**을 고려해야 합니다.

---

