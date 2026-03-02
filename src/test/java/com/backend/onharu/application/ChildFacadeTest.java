package com.backend.onharu.application;

import static com.backend.onharu.domain.support.error.ErrorType.Child.CHILD_NOT_FOUND;
import static com.backend.onharu.domain.support.error.ErrorType.Favorite.FAVORITE_NOT_FOUND;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_ALREADY_EXISTS;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_CHILD_ID_MISMATCH;
import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.favorite.dto.FavoriteCommand;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.favorite.FavoriteJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationHistoryJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.interfaces.api.common.util.PageableUtil;
import com.backend.onharu.interfaces.api.dto.ReservationStatusFilter;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ChildFacade 단위 테스트")
class ChildFacadeTest {

    @Autowired
    private ChildFacade childFacade;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Autowired
    private FavoriteJpaRepository favoriteJpaRepository;

    @Autowired
    private NotificationHistoryJpaRepository notificationHistoryJpaRepository;

    @Autowired
    private NotificationJpaRepository notificationJpaRepository;

    @BeforeEach
    public void setUp() {
        // 외래 키 제약 조건을 고려한 삭제 순서 (자식 → 부모)
        notificationHistoryJpaRepository.deleteAll();
        notificationJpaRepository.deleteAll();
        favoriteJpaRepository.deleteAll();
        reservationJpaRepository.deleteAll();
        storeScheduleJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        childJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
    }

    /**
     * 테스트용 User 생성 헬퍼 메서드 (아동용)
     */
    private User createTestUserForChild(String loginId, String name, String phone) {
        return userJpaRepository.save(
            User.builder()
                .loginId(loginId)
                .password("password123")
                .name(name)
                .phone(phone)
                .providerType(ProviderType.LOCAL)
                .userType(UserType.CHILD)
                .statusType(StatusType.ACTIVE)
                .build()
        );
    }

    /**
     * 테스트용 User 생성 헬퍼 메서드 (사업자용)
     */
    private User createTestUserForOwner(String loginId, String name, String phone) {
        return userJpaRepository.save(
            User.builder()
                .loginId(loginId)
                .password("password123")
                .name(name)
                .phone(phone)
                .userType(UserType.OWNER)
                .providerType(ProviderType.LOCAL)
                .statusType(StatusType.ACTIVE)
                .build()
        );
    }

    /**
     * 테스트용 Child 생성 헬퍼 메서드 (User, Level 과 함께 생성)
     */
    private Child createTestChild(String loginId, String name, String phone, String nickname, Boolean isVerified) {
        User user = createTestUserForChild(loginId, name, phone);

        return childJpaRepository.save(
            Child.builder()
                .user(user)
                .nickname(nickname)
                .isVerified(isVerified != null ? isVerified : true)
                .build()
        );
    }

    /**
     * 테스트용 Child 생성 헬퍼 메서드 (기본값 사용)
     */
    private Child createTestChild(String loginId, String name, String phone) {
        return createTestChild(loginId, name, phone, name + "닉네임", true);
    }

    /**
     * 테스트용 Level 생성 헬퍼 메서드
     */
    private Level createTestLevel(String levelName) {
        return levelJpaRepository.save(
                Level.builder()
                        .name(levelName)
                        .build()
        );
    }

    /**
     * 테스트용 Owner 생성 헬퍼 메서드 (User와 함께 생성)
     */
    private Owner createTestOwner(String loginId, String name, String phone, String levelName, String businessNumber) {
        User user = createTestUserForOwner(loginId, name, phone);
        Level level = createTestLevel(levelName);

        return ownerJpaRepository.save(
            Owner.builder()
                .user(user)
                .level(level)
                .businessNumber(businessNumber)
                .build()
        );
    }

    /**
     * 테스트용 Category 생성 헬퍼 메서드
     */
    private Category createTestCategory(String name) {
        return categoryJpaRepository.save(Category.builder().name(name).build());
    }

    /**
     * 테스트용 Store 생성 헬퍼 메서드
     */
    private Store createTestStore(String name, Owner owner, Category category) {
        return storeJpaRepository.save(Store.builder()
            .name(name)
            .owner(owner)
            .category(category)
            .address("서울시 강남구")
            .phone("0212345678")
            .introduction("따뜻한 마음으로 환영합니다!")
            .intro("따뜻한 한 끼 식사")
            .isOpen(true)
            .build());
    }

    /**
     * 테스트용 StoreSchedule 생성 헬퍼 메서드
     */
    private StoreSchedule createTestStoreSchedule(Store store, int startHour, int endHour) {
        return storeScheduleJpaRepository.save(
            StoreSchedule.builder()
                .store(store)
                .scheduleDate(LocalDate.now().plusDays(1))
                .startTime(LocalTime.of(startHour, 0))
                .endTime(LocalTime.of(endHour, 0))
                .maxPeople(10)
                .build()
        );
    }

    @Nested
    @DisplayName("예약 하기 테스트")
    class ReserveTest {
        
        @Test
        @DisplayName("예약 생성 성공")
        @Rollback(value = false)
        public void shouldCreateReservation() {
            // given
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "비기너", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);
            
            CreateReservationCommand command = new CreateReservationCommand(
                child.getId(),
                storeSchedule.getId(),
                2
            );

            // when
            childFacade.reserve(command); // 예약 생성

            // then
            Reservation reservation = reservationJpaRepository.getLatestByStoreScheduleId(storeSchedule.getId())
                .orElse(null); // 예약 조회
            assertThat(reservation).isNotNull();
            assertThat(reservation.getChild().getId()).isEqualTo(child.getId());
            assertThat(reservation.getStoreSchedule().getId()).isEqualTo(storeSchedule.getId());
            assertThat(reservation.getPeople()).isEqualTo(2);
            assertThat(reservation.getStatus()).isEqualTo(ReservationType.WAITING);
            
            System.out.println("✅ 예약 생성 성공 - Reservation ID: " + reservation.getId());
            System.out.println("   - 아동 ID: " + child.getId());
            System.out.println("   - 가게 일정 ID: " + storeSchedule.getId());
            System.out.println("   - 인원 수: " + reservation.getPeople());
        }

        @Test
        @DisplayName("예약 생성 실패 - 이미 예약된 일정")
        public void shouldThrowExceptionWhenScheduleAlreadyReserved() {
            // given
            Child child1 = createTestChild("test_child1", "테스트 아동1", "01012345678");
            Child child2 = createTestChild("test_child2", "테스트 아동2", "01087654321");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹2", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);
            
            // 첫 번째 예약 생성
            CreateReservationCommand firstCommand = new CreateReservationCommand(
                child1.getId(),
                storeSchedule.getId(),
                2
            );
            childFacade.reserve(firstCommand);

            // 두 번째 예약 시도
            CreateReservationCommand secondCommand = new CreateReservationCommand(
                child2.getId(),
                storeSchedule.getId(),
                3
            );

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> childFacade.reserve(secondCommand)
            );
            
            assertThat(exception.getErrorType()).isEqualTo(RESERVATION_ALREADY_EXISTS);
        }

        @Test
        @DisplayName("예약 생성 실패 - 최대 인원 초과")
        public void shouldThrowExceptionWhenPeopleExceedsMaxPeople() {
            // given: maxPeople=5인 일정에 10명 예약 시도
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹3", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = storeScheduleJpaRepository.save(
                StoreSchedule.builder()
                    .store(store)
                    .scheduleDate(LocalDate.now().plusDays(1))
                    .startTime(LocalTime.of(10, 0))
                    .endTime(LocalTime.of(11, 0))
                    .maxPeople(5)
                    .build()
            );

            CreateReservationCommand command = new CreateReservationCommand(
                child.getId(),
                storeSchedule.getId(),
                10  // maxPeople(5) 초과
            );

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> childFacade.reserve(command)
            );

            assertThat(exception.getErrorType()).isEqualTo(ErrorType.Reservation.RESERVATION_PEOPLE_EXCEEDS_MAX);
        }
    }

    @Nested
    @DisplayName("예약 취소 테스트")
    class CancelReservationTest {
        
        @Test
        @DisplayName("예약 취소 성공")
        @Rollback(value = false)
        public void shouldCancelReservation() {
            // given
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹4", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);
            
            Reservation reservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );
            
            CancelReservationCommand command = new CancelReservationCommand(
                reservation.getId(),
                UserType.CHILD,
                "일정 변경으로 인한 취소"
            );

            // when
            childFacade.cancelReservation(command, child.getId());

            // then
            Reservation canceledReservation = reservationJpaRepository.findById(reservation.getId())
                .orElse(null);
            assertThat(canceledReservation).isNotNull();
            assertThat(canceledReservation.getStatus()).isEqualTo(ReservationType.CANCELED);
            assertThat(canceledReservation.getCancelReason()).isEqualTo("일정 변경으로 인한 취소");
            assertThat(canceledReservation.getCancelRequestedBy()).isEqualTo(UserType.CHILD);
            
            System.out.println("✅ 예약 취소 성공 - Reservation ID: " + canceledReservation.getId());
            System.out.println("   - 상태: " + canceledReservation.getStatus());
            System.out.println("   - 취소 사유: " + canceledReservation.getCancelReason());
        }

        @Test
        @DisplayName("예약 취소 실패 - 다른 아동의 예약")
        public void shouldThrowExceptionWhenReservationBelongsToOtherChild() {
            // given
            Child child1 = createTestChild("test_child1", "테스트 아동1", "01012345678"); // 아동1 생성
            Child child2 = createTestChild("test_child2", "테스트 아동2", "01087654321"); // 아동2 생성
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹5", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11); // 가게 일정 생성 (10시 ~ 11시)
            
            Reservation reservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            ); // 아동1 이 가게에 예약 생성
            
            CancelReservationCommand command = new CancelReservationCommand(
                reservation.getId(),
                UserType.CHILD,
                "일정 변경으로 인한 취소"
            );

            assertThat(command.cancelRequestedBy()).isEqualTo(UserType.CHILD);

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> childFacade.cancelReservation(command, child2.getId()) // 아동2가 예약 취소 시도
            );
            
            assertThat(exception.getErrorType()).isEqualTo(RESERVATION_CHILD_ID_MISMATCH);
        }
    }

    @Nested
    @DisplayName("내가 신청한 예약 목록 조회 테스트")
    class GetMyBookingsTest {
        
        @Test
        @DisplayName("내가 신청한 예약 목록 조회 성공")
        @Rollback(value = false)
        public void shouldGetMyBookings() {
            // given
            Child child1 = createTestChild("test_child1", "테스트 아동1", "01012345678");
            Child child2 = createTestChild("test_child2", "테스트 아동2", "01087654321");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹6", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            
            StoreSchedule schedule1 = createTestStoreSchedule(store, 10, 11);
            StoreSchedule schedule2 = createTestStoreSchedule(store, 14, 15);
            StoreSchedule schedule3 = createTestStoreSchedule(store, 16, 17);
            
            // child1의 예약 2개
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(schedule1)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(schedule2)
                    .people(3)
                    .status(ReservationType.WAITING)
                    .build()
            );
            
            // child2의 예약 1개
            reservationJpaRepository.save(
                Reservation.builder()
                    .child(child2)
                    .storeSchedule(schedule3)
                    .people(1)
                    .status(ReservationType.WAITING)
                    .build()
            );

            Pageable pageable = PageableUtil.ofOneBased(
                1,
                10,
                "id",
                "desc"
            );

            // when
            List<Reservation> myBookings = childFacade.getMyBookings(child1.getId(), ReservationStatusFilter.ALL, pageable).getContent();

            // then
            assertThat(myBookings).isNotNull();
            assertThat(myBookings.size()).isEqualTo(2);
            assertThat(myBookings).allMatch(r -> r.getChild().getId().equals(child1.getId()));
            
            System.out.println("✅ 내가 신청한 예약 목록 조회 성공");
            System.out.println("   - 아동 ID: " + child1.getId());
            System.out.println("   - 예약 개수: " + myBookings.size());
        }

        @Test
        @DisplayName("예약이 없을 때 빈 목록 반환")
        public void shouldReturnEmptyListWhenNoReservations() {
            // given
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");

            Pageable pageable = PageableUtil.ofOneBased(
                1,
                10,
                "id",
                "desc"
            );

            // when
            List<Reservation> myBookings = childFacade.getMyBookings(child.getId(), ReservationStatusFilter.ALL, pageable).getContent();

            // then
            assertThat(myBookings).isNotNull();
            assertThat(myBookings).isEmpty();
        }
    }

    @Nested
    @DisplayName("내가 신청한 특정 예약의 상세 정보 조회 테스트")
    class GetMyBookingTest {
        
        @Test
        @DisplayName("내가 신청한 특정 예약의 상세 정보 조회 성공")
        @Rollback(value = false)
        public void shouldGetMyBooking() {
            // given
            Child child = createTestChild("test_child", "테스트 아동", "01012345678");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹7", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);
            
            Reservation reservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            );

            // when
            Reservation result = childFacade.getMyBooking(reservation.getId(), child.getId());

            // then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(reservation.getId());
            assertThat(result.getChild().getId()).isEqualTo(child.getId());
            assertThat(result.getStoreSchedule().getId()).isEqualTo(storeSchedule.getId());
            assertThat(result.getPeople()).isEqualTo(2);
            
            System.out.println("✅ 내가 신청한 특정 예약의 상세 정보 조회 성공");
            System.out.println("   - 예약 ID: " + result.getId());
            System.out.println("   - 아동 ID: " + child.getId());
        }

        @Test
        @DisplayName("다른 아동의 예약 조회 시 예외 발생")
        public void shouldThrowExceptionWhenReservationBelongsToOtherChild() {
            // given
            Child child1 = createTestChild("test_child1", "테스트 아동1", "01012345678"); // 아동1 생성
            Child child2 = createTestChild("test_child2", "테스트 아동2", "01087654321"); // 아동2 생성
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01011112222", "새싹8", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11); // 가게 일정 생성 (10시 ~ 11시)
            
            Reservation reservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.WAITING)
                    .build()
            ); // 아동1 이 가게에 예약 생성

            // when & then
            CoreException exception = Assertions.assertThrows(
                CoreException.class,
                () -> childFacade.getMyBooking(reservation.getId(), child2.getId()) // 아동2가 예약 조회 시도
            );
            
            assertThat(exception.getErrorType()).isEqualTo(RESERVATION_CHILD_ID_MISMATCH);
        }

        @Test
        @DisplayName("동일 스케줄에 취소+재예약 시 각 아동이 자기 예약만 조회")
        @Rollback(value = false)
        public void shouldGetOwnReservationsWhenSameScheduleHasCanceledAndWaiting() {
            // given: schedule에 child1 CANCELED → child2 WAITING (취소 후 재예약)
            Child child1 = createTestChild("test_child_canceled", "취소한 아동", "01011111111");
            Child child2 = createTestChild("test_child_rebooked", "재예약한 아동", "01022222222");
            Owner owner = createTestOwner("test_owner", "테스트 사업자", "01033334444", "새싹9", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);
            StoreSchedule storeSchedule = createTestStoreSchedule(store, 10, 11);

            Reservation canceledReservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child1)
                    .storeSchedule(storeSchedule)
                    .people(2)
                    .status(ReservationType.CANCELED)
                    .build()
            );
            Reservation waitingReservation = reservationJpaRepository.save(
                Reservation.builder()
                    .child(child2)
                    .storeSchedule(storeSchedule)
                    .people(3)
                    .status(ReservationType.WAITING)
                    .build()
            );

            Pageable pageable = PageableUtil.ofOneBased(1, 10, "id", "desc");

            // when
            List<Reservation> child1Bookings = childFacade.getMyBookings(child1.getId(), ReservationStatusFilter.ALL, pageable).getContent();
            List<Reservation> child2Bookings = childFacade.getMyBookings(child2.getId(), ReservationStatusFilter.ALL, pageable).getContent();

            // then: 각 아동은 자기 예약만 조회
            assertThat(child1Bookings).hasSize(1);
            assertThat(child1Bookings.get(0).getId()).isEqualTo(canceledReservation.getId());
            assertThat(child1Bookings.get(0).getStatus()).isEqualTo(ReservationType.CANCELED);
            assertThat(child1Bookings.get(0).getChild().getId()).isEqualTo(child1.getId());

            assertThat(child2Bookings).hasSize(1);
            assertThat(child2Bookings.get(0).getId()).isEqualTo(waitingReservation.getId());
            assertThat(child2Bookings.get(0).getStatus()).isEqualTo(ReservationType.WAITING);
            assertThat(child2Bookings.get(0).getChild().getId()).isEqualTo(child2.getId());

            // 각자 자신의 예약 상세 조회도 가능
            Reservation child1Detail = childFacade.getMyBooking(canceledReservation.getId(), child1.getId());
            assertThat(child1Detail.getStatus()).isEqualTo(ReservationType.CANCELED);

            Reservation child2Detail = childFacade.getMyBooking(waitingReservation.getId(), child2.getId());
            assertThat(child2Detail.getStatus()).isEqualTo(ReservationType.WAITING);
        }
    }

    @Nested
    @DisplayName("찜 등록 테스트")
    class CreateFavoriteTest {

        @Test
        @DisplayName("찜 등록 성공")
        void shouldCreateFavorite() {
            // GIVEN
            Child child = createTestChild("child@test.com", "아동테스트", "01000000001");
            Owner owner = createTestOwner("owner@test.com", "사업자테스트", "01022220002", "새싹10", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);

            FavoriteCommand.CreateFavoriteCommand command = new FavoriteCommand.CreateFavoriteCommand(child.getId(), store.getId());

            // WHEN
            Favorite favorite = childFacade.createFavorite(command);

            // THEN
            assertThat(favorite).isNotNull();
            assertThat(favorite.getChild().getId()).isEqualTo(child.getId());
            assertThat(favorite.getStore().getId()).isEqualTo(store.getId());

            assertThat(favoriteJpaRepository.findAll()).hasSize(1);
        }

        @Test
        @DisplayName("찜 등록 실패 - 존재하지 않는 아동일 경우")
        void shouldThrowExceptionWhenChildNotFound() {
            Long childId = 123456789L;
            Owner owner = createTestOwner("owner@test.com", "사업자테스트", "01022220001", "새싹11", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);

            FavoriteCommand.CreateFavoriteCommand command = new FavoriteCommand.CreateFavoriteCommand(childId, store.getId());

            CoreException exception = Assertions.assertThrows(CoreException.class, () -> childFacade.createFavorite(command));

            assertThat(exception.getErrorType()).isEqualTo(CHILD_NOT_FOUND);
        }
    }

    @Nested
    @DisplayName("찜 목록 조회 테스트")
    class GetMyFavoritesTest {

        @Test
        @DisplayName("찜 목록 조회 성공")
        void shouldGetMyFavorites() {
            // GIVEN
            Child child = createTestChild("child@test.com", "아동테스트", "01000000001");
            Owner owner1 = createTestOwner("owner1@test.com", "사업자테스트1", "01022220001", "새싹12", "1234567890");
            Owner owner2 = createTestOwner("owner2@test.com", "사업자테스트2", "01022220002", "새싹13", "1234567891");
            Category category = createTestCategory("식당");
            Store store1 = createTestStore("테스트 가게1", owner1, category);
            Store store2 = createTestStore("테스트 가게2", owner2, category);

            favoriteJpaRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store1)
                            .build()
            );
            favoriteJpaRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store2)
                            .build()
            );
            FavoriteQuery.FindFavoritesByChildIdQuery query = new FavoriteQuery.FindFavoritesByChildIdQuery(child.getId());

            Pageable pageable = PageRequest.of(0, 10);

            // WHEN
            Page<Favorite> favorites = childFacade.getMyFavorites(query, pageable);

            // THEN
            assertThat(favorites.getTotalElements()).isEqualTo(2);
            assertThat(favorites.getContent()).hasSize(2);
            assertThat(favorites.getContent()).allMatch(f -> f.getChild().getId().equals(child.getId()));
        }

        @Test
        @DisplayName("찜 목록 조회 실패 - 찜하기가 없을 경우 빈 목록 반환")
        void shouldThrowExceptionWhenEmptyList() {
            // GIVEN
            Child child = createTestChild("child@test.com", "아동테스트", "01000000001");

            FavoriteQuery.FindFavoritesByChildIdQuery query = new FavoriteQuery.FindFavoritesByChildIdQuery(child.getId());

            Pageable pageable = PageRequest.of(0, 10);

            // WHEN
            Page<Favorite> favorites = childFacade.getMyFavorites(query, pageable);

            // THEN
            assertThat(favorites.getTotalElements()).isZero();
            assertThat(favorites.getContent()).isEmpty();
        }
    }

    @Nested
    @DisplayName("찜 취소 테스트")
    class DeleteFavoriteTest {

        @Test
        @DisplayName("찜 취소 성공")
        void shouldDeleteFavorite() {
            // GIVEN
            Child child = createTestChild("child@test.com", "아동테스트", "01000000001");
            Owner owner = createTestOwner("owner@test.com", "사업자테스트", "01022220002", "새싹", "1234567890");
            Category category = createTestCategory("식당");
            Store store = createTestStore("테스트 가게", owner, category);

            Favorite favorite = favoriteJpaRepository.save(
                    Favorite.builder()
                            .child(child)
                            .store(store)
                            .build()
            );

            FavoriteCommand.DeleteFavoriteCommand command = new FavoriteCommand.DeleteFavoriteCommand(child.getId(), favorite.getId());

            // WHEN
            childFacade.deleteFavorite(command);

            // THEN
            assertThat(favoriteJpaRepository.findById(favorite.getId())).isEmpty();
        }

        @Test
        @DisplayName("찜 취소 실패 - 존재하지 않는 찜하기를 삭제할 경우")
        void shouldThrowExceptionWhenFavoriteNotFound() {
            // GIVEN
            Long childId = 123456789L;
            Child child = createTestChild("child@test.com", "아동테스트", "01000000001");

            FavoriteCommand.DeleteFavoriteCommand command = new FavoriteCommand.DeleteFavoriteCommand(child.getId(), childId);

            // WHEN
            CoreException exception = Assertions.assertThrows(CoreException.class, () -> childFacade.deleteFavorite(command));

            assertThat(exception.getErrorType()).isEqualTo(FAVORITE_NOT_FOUND);
        }
    }
}
