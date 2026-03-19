package com.backend.onharu.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.event.listener.ReservationEventListner;
import com.backend.onharu.event.model.ReservationEvent;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.favorite.FavoriteJpaRepository;
import com.backend.onharu.infra.db.file.FileJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationHistoryJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

/**
 * 예약 이벤트 리스너 통합 테스트입니다.
 *
 * 이벤트 처리 시 사장(Owner)·아동(Child)용 알림 메시지가 DB(notification_histories)에
 * 올바르게 저장되는지 검증합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("ReservationEventListner DB 저장 통합 테스트")
class ReservationEventListnerIntegrationTest {

    private static final String TEST_STORE_NAME = "테스트 가게";

    @Autowired private ReservationJpaRepository reservationJpaRepository;
    @Autowired private ChildJpaRepository childJpaRepository;
    @Autowired private UserJpaRepository userJpaRepository;
    @Autowired private StoreScheduleJpaRepository storeScheduleJpaRepository;
    @Autowired private StoreJpaRepository storeJpaRepository;
    @Autowired private OwnerJpaRepository ownerJpaRepository;
    @Autowired private CategoryJpaRepository categoryJpaRepository;
    @Autowired private LevelJpaRepository levelJpaRepository;
    @Autowired private FileJpaRepository fileJpaRepository;
    @Autowired private FavoriteJpaRepository favoriteJpaRepository;
    @Autowired private TagJpaRepository tagJpaRepository;
    @Autowired private NotificationHistoryJpaRepository notificationHistoryJpaRepository;
    @Autowired private NotificationJpaRepository notificationJpaRepository;
    @Autowired private ReservationEventListner reservationEventListner;

    @BeforeEach
    public void setUp() {
        notificationHistoryJpaRepository.deleteAll();
        notificationJpaRepository.deleteAll();
        reservationJpaRepository.deleteAll();
        storeScheduleJpaRepository.deleteAll();
        fileJpaRepository.deleteAll();
        favoriteJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        tagJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        childJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        levelJpaRepository.deleteAll();
    }

    private User createTestUser(String loginId, String name, String phone, UserType userType) {
        return userJpaRepository.save(User.builder()
                .loginId(loginId)
                .password("password123")
                .name(name)
                .phone(phone)
                .providerType(ProviderType.LOCAL)
                .userType(userType)
                .statusType(StatusType.ACTIVE)
                .build());
    }

    private Level createTestLevel(String name) {
        return levelJpaRepository.save(Level.builder().name(name).build());
    }

    private Owner createTestOwner(String loginId, String name, String phone) {
        User user = createTestUser(loginId, name, phone, UserType.OWNER);
        Level level = createTestLevel("새싹");
        return ownerJpaRepository.save(Owner.builder()
                .user(user)
                .level(level)
                .businessNumber("1234567890")
                .build());
    }

    private Child createTestChild(String loginId, String name, String phone) {
        User user = createTestUser(loginId, name, phone, UserType.CHILD);
        return childJpaRepository.save(Child.builder()
                .user(user)
                .nickname(loginId + "_닉네임")
                .isVerified(true)
                .build());
    }

    private Category createTestCategory() {
        return categoryJpaRepository.save(Category.builder().name("음식점").build());
    }

    private Store createTestStore(Owner owner, Category category) {
        return storeJpaRepository.save(Store.builder()
                .owner(owner)
                .category(category)
                .name(TEST_STORE_NAME)
                .address("서울시 강남구 테헤란로 1")
                .phone("0212345678")
                .isOpen(true)
                .isSharing(true)
                .build());
    }

    private StoreSchedule createTestStoreSchedule(Store store) {
        return storeScheduleJpaRepository.save(StoreSchedule.builder()
                .store(store)
                .scheduleDate(LocalDate.now())
                .startTime(LocalTime.of(12, 0))
                .endTime(LocalTime.of(14, 0))
                .maxPeople(10)
                .build());
    }

    private Reservation createTestReservation(Child child, StoreSchedule storeSchedule) {
        return reservationJpaRepository.save(Reservation.builder()
                .child(child)
                .storeSchedule(storeSchedule)
                .people(2)
                .reservationAt(LocalDateTime.now())
                .status(ReservationType.WAITING)
                .build());
    }

    @Nested
    @DisplayName("사장(Owner) 메시지 DB 저장 테스트")
    class OwnerMessageDbSaveTest {

        @Test
        @DisplayName("RESERVATION_CREATED - 사장용 메시지가 DB에 저장된다")
        void shouldSaveOwnerMessageToDb_WhenReservationCreated() {
            // given
            Owner owner = createTestOwner("owner_created@test.com", "사장", "01011111111");
            Child child = createTestChild("child_created@test.com", "아동", "01022222222");
            Category category = createTestCategory();
            Store store = createTestStore(owner, category);
            StoreSchedule schedule = createTestStoreSchedule(store);
            Reservation reservation = createTestReservation(child, schedule);

            ReservationEvent event = new ReservationEvent(
                    reservation.getId(),
                    owner.getId(),
                    child.getId(),
                    NotificationHistoryType.RESERVATION_CREATED
            );

            // when
            reservationEventListner.handleReservationEvent(event);

            // then
            List<NotificationHistory> ownerNotifications = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(owner.getUser().getId(), PageRequest.of(0, 10))
                    .getContent();

            assertThat(ownerNotifications).hasSize(1);
            assertThat(ownerNotifications.get(0).getMessage())
                    .isEqualTo("새로운 예약이 확정되었습니다. 예약 번호: " + reservation.getId());
            assertThat(ownerNotifications.get(0).getTitle()).isEqualTo("매장 관리 알림");
            assertThat(ownerNotifications.get(0).getType()).isEqualTo(NotificationHistoryType.RESERVATION_CREATED);
        }

        @Test
        @DisplayName("RESERVATION_CANCELED - 사장용 메시지가 DB에 저장된다")
        void shouldSaveOwnerMessageToDb_WhenReservationCanceled() {
            // given
            Owner owner = createTestOwner("owner_cancel@test.com", "사장", "01033333333");
            Child child = createTestChild("child_cancel@test.com", "아동", "01044444444");
            Category category = createTestCategory();
            Store store = createTestStore(owner, category);
            StoreSchedule schedule = createTestStoreSchedule(store);
            Reservation reservation = createTestReservation(child, schedule);

            ReservationEvent event = new ReservationEvent(
                    reservation.getId(),
                    owner.getId(),
                    child.getId(),
                    NotificationHistoryType.RESERVATION_CANCELED
            );

            // when
            reservationEventListner.handleReservationEvent(event);

            // then
            List<NotificationHistory> ownerNotifications = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(owner.getUser().getId(), PageRequest.of(0, 10))
                    .getContent();

            assertThat(ownerNotifications).hasSize(1);
            assertThat(ownerNotifications.get(0).getMessage())
                    .isEqualTo("[" + TEST_STORE_NAME + "] 예약이 취소되었습니다.");
            assertThat(ownerNotifications.get(0).getTitle()).isEqualTo("매장 관리 알림");
        }
    }

    @Nested
    @DisplayName("아동(Child) 메시지 DB 저장 테스트")
    class ChildMessageDbSaveTest {

        @Test
        @DisplayName("RESERVATION_CANCELED - 아동용 메시지가 DB에 저장된다")
        void shouldSaveChildMessageToDb_WhenReservationCanceled() {
            // given
            Owner owner = createTestOwner("owner_cancel2@test.com", "사장", "01055555555");
            Child child = createTestChild("child_cancel2@test.com", "아동", "01066666666");
            Category category = createTestCategory();
            Store store = createTestStore(owner, category);
            StoreSchedule schedule = createTestStoreSchedule(store);
            Reservation reservation = createTestReservation(child, schedule);

            ReservationEvent event = new ReservationEvent(
                    reservation.getId(),
                    owner.getId(),
                    child.getId(),
                    NotificationHistoryType.RESERVATION_CANCELED
            );

            // when
            reservationEventListner.handleReservationEvent(event);

            // then
            List<NotificationHistory> childNotifications = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(child.getUser().getId(), PageRequest.of(0, 10))
                    .getContent();

            assertThat(childNotifications).hasSize(1);
            assertThat(childNotifications.get(0).getMessage())
                    .isEqualTo("[" + TEST_STORE_NAME + "] 예약 취소가 잘 처리됐어요. 다음에 또 봐요!");
            assertThat(childNotifications.get(0).getTitle()).isEqualTo("예약 안내 소식");
            assertThat(childNotifications.get(0).getType()).isEqualTo(NotificationHistoryType.RESERVATION_CANCELED);
        }

        @Test
        @DisplayName("RESERVATION_COMPLETED - 아동용 메시지가 DB에 저장된다")
        void shouldSaveChildMessageToDb_WhenReservationCompleted() {
            // given
            Owner owner = createTestOwner("owner_complete@test.com", "사장", "01077777777");
            Child child = createTestChild("child_complete@test.com", "아동", "01088888888");
            Category category = createTestCategory();
            Store store = createTestStore(owner, category);
            StoreSchedule schedule = createTestStoreSchedule(store);
            Reservation reservation = createTestReservation(child, schedule);

            ReservationEvent event = new ReservationEvent(
                    reservation.getId(),
                    owner.getId(),
                    child.getId(),
                    NotificationHistoryType.RESERVATION_COMPLETED
            );

            // when
            reservationEventListner.handleReservationEvent(event);

            // then
            List<NotificationHistory> childNotifications = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(child.getUser().getId(), PageRequest.of(0, 10))
                    .getContent();

            assertThat(childNotifications).hasSize(1);
            assertThat(childNotifications.get(0).getMessage())
                    .isEqualTo("[" + TEST_STORE_NAME + "] 예약이 완료됐어요! 매장에서 만나요.");
            assertThat(childNotifications.get(0).getTitle()).isEqualTo("예약 안내 소식");
        }
    }

    @Nested
    @DisplayName("사장·아동 동시 저장 테스트")
    class OwnerAndChildSimultaneousSaveTest {

        @Test
        @DisplayName("RESERVATION_CANCELED - 사장·아동 각각 다른 메시지가 DB에 저장된다")
        void shouldSaveBothOwnerAndChildMessagesToDb_WhenReservationCanceled() {
            // given
            Owner owner = createTestOwner("owner_both@test.com", "사장", "01099991111");
            Child child = createTestChild("child_both@test.com", "아동", "01099992222");
            Category category = createTestCategory();
            Store store = createTestStore(owner, category);
            StoreSchedule schedule = createTestStoreSchedule(store);
            Reservation reservation = createTestReservation(child, schedule);

            ReservationEvent event = new ReservationEvent(
                    reservation.getId(),
                    owner.getId(),
                    child.getId(),
                    NotificationHistoryType.RESERVATION_CANCELED
            );

            // when
            reservationEventListner.handleReservationEvent(event);

            // then - 사장용
            List<NotificationHistory> ownerNotifications = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(owner.getUser().getId(), PageRequest.of(0, 10))
                    .getContent();
            assertThat(ownerNotifications).hasSize(1);
            assertThat(ownerNotifications.get(0).getMessage())
                    .isEqualTo("[" + TEST_STORE_NAME + "] 예약이 취소되었습니다.");

            // then - 아동용
            List<NotificationHistory> childNotifications = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(child.getUser().getId(), PageRequest.of(0, 10))
                    .getContent();
            assertThat(childNotifications).hasSize(1);
            assertThat(childNotifications.get(0).getMessage())
                    .isEqualTo("[" + TEST_STORE_NAME + "] 예약 취소가 잘 처리됐어요. 다음에 또 봐요!");

            // 전체 알림 2건 확인
            assertThat(notificationHistoryJpaRepository.count()).isEqualTo(2);
        }

        @Test
        @DisplayName("RESERVATION_CREATED - 사장용만 저장되고 아동용은 저장되지 않는다")
        void shouldSaveOnlyOwnerMessageToDb_WhenReservationCreated() {
            // given - RESERVATION_CREATED는 아동 메시지가 null
            Owner owner = createTestOwner("owner_only@test.com", "사장", "01099993333");
            Child child = createTestChild("child_only@test.com", "아동", "01099994444");
            Category category = createTestCategory();
            Store store = createTestStore(owner, category);
            StoreSchedule schedule = createTestStoreSchedule(store);
            Reservation reservation = createTestReservation(child, schedule);

            ReservationEvent event = new ReservationEvent(
                    reservation.getId(),
                    owner.getId(),
                    child.getId(),
                    NotificationHistoryType.RESERVATION_CREATED
            );

            // when
            reservationEventListner.handleReservationEvent(event);

            // then - 사장용만 1건 (RESERVATION_CREATED는 아동 메시지 없음)
            assertThat(notificationHistoryJpaRepository.count()).isEqualTo(1);

            List<NotificationHistory> ownerNotifications = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(owner.getUser().getId(), PageRequest.of(0, 10))
                    .getContent();
            assertThat(ownerNotifications.get(0).getMessage())
                    .isEqualTo("새로운 예약이 확정되었습니다. 예약 번호: " + reservation.getId());

            // 아동용 알림은 없음
            List<NotificationHistory> childNotifications = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(child.getUser().getId(), PageRequest.of(0, 10))
                    .getContent();
            assertThat(childNotifications).isEmpty();
        }
    }
}
