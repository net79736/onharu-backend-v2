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

