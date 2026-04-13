package com.backend.onharu.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

@SpringBootTest
@ActiveProfiles("test")
class ReservationDistributedLockConcurrencyTest {

    @Autowired
    private ChildFacade childFacade;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @BeforeEach
    void setUp() {
        reservationJpaRepository.deleteAll();
        storeScheduleJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        childJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        levelJpaRepository.deleteAll();
    }

    private User createTestUser(String loginId, String name, String phone, UserType userType) {
        return userJpaRepository.save(
                User.builder()
                        .loginId(loginId)
                        .password("password123")
                        .name(name)
                        .phone(phone)
                        .providerType(ProviderType.LOCAL)
                        .userType(userType)
                        .statusType(StatusType.ACTIVE)
                        .build());
    }

    private Child createTestChild(int idx) {
        User user = createTestUser("child_" + idx, "아동" + idx, "0100000" + String.format("%04d", idx), UserType.CHILD);
        return childJpaRepository.save(
                Child.builder()
                        .user(user)
                        .nickname("아동닉" + idx)
                        .isVerified(true)
                        .build());
    }

    private Owner createTestOwner() {
        Level level = levelJpaRepository.save(Level.builder().name("테스트레벨").build());
        User user = createTestUser("owner_1", "사업자", "01099990000", UserType.OWNER);
        return ownerJpaRepository.save(
                Owner.builder()
                        .user(user)
                        .level(level)
                        .businessNumber("1234567890")
                        .build());
    }

    private Store createTestStore(Owner owner) {
        Category category = categoryJpaRepository.save(Category.builder().name("식당").build());
        return storeJpaRepository.save(
                Store.builder()
                        .name("테스트가게")
                        .owner(owner)
                        .category(category)
                        .address("서울시 강남구")
                        .phone("0212345678")
                        .intro("따뜻한 한 끼 식사")
                        .introduction("따뜻한 마음으로 환영합니다!")
                        .isOpen(true)
                        .build());
    }

    private StoreSchedule createTestStoreSchedule(Store store) {
        return storeScheduleJpaRepository.save(
                StoreSchedule.builder()
                        .store(store)
                        .scheduleDate(LocalDate.now().plusDays(1))
                        .startTime(LocalTime.of(10, 0))
                        .endTime(LocalTime.of(11, 0))
                        .maxPeople(10)
                        .build());
    }

    @Test
    @DisplayName("여러 사용자가 동시에 분산 락(Redis Lock)으로 동일 일정에 예약 시도할 때, 1건만 성공한다 - 분산 락 동시성 테스트")
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    void shouldCreateOnlyOneReservationWithDistributedLock() {
        // given
        final int REQUEST_COUNT = 10;
        Owner owner = createTestOwner();
        Store store = createTestStore(owner);
        StoreSchedule schedule = createTestStoreSchedule(store);
        List<Child> children = IntStream.range(0, REQUEST_COUNT)
                .mapToObj(this::createTestChild)
                .toList();

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // when
        List<CompletableFuture<Void>> futures = IntStream.range(0, REQUEST_COUNT)
                .mapToObj(i -> CompletableFuture.runAsync(() -> {
                    try {
                        childFacade.reserve(new CreateReservationCommand(children.get(i).getId(), schedule.getId(), 2));
                        successCount.incrementAndGet();
                    } catch (CoreException e) {
                        failCount.incrementAndGet();
                    } catch (Exception e) {
                        failCount.incrementAndGet();
                    }
                }))
                .toList();

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // then
        List<Reservation> reservations = reservationJpaRepository.findAll();
        assertThat(reservations).hasSize(1);
        assertThat(reservations.get(0).getStoreSchedule().getId()).isEqualTo(schedule.getId());
        assertThat(reservations.get(0).getStatus()).isEqualTo(ReservationType.WAITING);
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(REQUEST_COUNT - 1);
    }
}

