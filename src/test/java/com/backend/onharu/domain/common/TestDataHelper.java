package com.backend.onharu.domain.common;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.chat.ChatMessageJpaRepository;
import com.backend.onharu.infra.db.chat.ChatParticipantJpaRepository;
import com.backend.onharu.infra.db.chat.ChatRoomJpaRepository;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.email.EmailAuthenticationJpaRepository;
import com.backend.onharu.infra.db.favorite.FavoriteJpaRepository;
import com.backend.onharu.infra.db.file.FileJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationHistoryJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationJpaRepository;
import com.backend.onharu.infra.db.outbox.OutboxEventJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.review.ReviewJpaRepository;
import com.backend.onharu.infra.db.store.BusinessHoursJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreCountJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.infra.db.user.UserOAuthJpaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * 테스트 데이터 생성/정리 헬퍼 클래스
 * <p>
 * 여러 테스트 클래스에서 공통으로 사용할 수 있는 테스트 데이터 생성 메서드와
 * {@link #cleanAll()} 을 통한 공통 cleanup 을 제공합니다.
 *
 * <p>기존 각 테스트에서 개별적으로 FK 순서에 맞춰 {@code deleteAll()} 을 나열하던
 * 코드를 이 헬퍼 한 곳으로 집약합니다. 테스트는 {@code @BeforeEach} 에서
 * {@code testDataHelper.cleanAll()} 만 호출하면 됩니다.</p>
 */
@Component
public class TestDataHelper {

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private UserOAuthJpaRepository userOAuthJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private StoreCountJpaRepository storeCountJpaRepository;

    @Autowired
    private BusinessHoursJpaRepository businessHoursJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Autowired
    private TagJpaRepository tagJpaRepository;

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private ReviewJpaRepository reviewJpaRepository;

    @Autowired
    private FavoriteJpaRepository favoriteJpaRepository;

    @Autowired
    private FileJpaRepository fileJpaRepository;

    @Autowired
    private NotificationJpaRepository notificationJpaRepository;

    @Autowired
    private NotificationHistoryJpaRepository notificationHistoryJpaRepository;

    @Autowired
    private ChatMessageJpaRepository chatMessageJpaRepository;

    @Autowired
    private ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Autowired
    private ChatRoomJpaRepository chatRoomJpaRepository;

    @Autowired
    private EmailAuthenticationJpaRepository emailAuthenticationJpaRepository;

    @Autowired
    private OutboxEventJpaRepository outboxEventJpaRepository;

    /**
     * 모든 엔티티를 FK 관계를 고려해 자식 → 부모 순서로 삭제합니다.
     *
     * <p>테스트 클래스 간 H2 인메모리 DB 공유로 인한 데이터 누적을 방지합니다.
     * {@code REQUIRES_NEW} 전파로 호출 트랜잭션과 분리된 독립 트랜잭션에서
     * 커밋되므로, {@code @Transactional} 테스트 메서드의 {@code @BeforeEach}
     * 에서 호출해도 테스트 메서드의 롤백과 관계없이 이전 클래스에서 남은
     * 데이터를 확실히 지워줍니다.</p>
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void cleanAll() {
        // 채팅: 메시지 → 참가자 → 방
        chatMessageJpaRepository.deleteAll();
        chatParticipantJpaRepository.deleteAll();
        chatRoomJpaRepository.deleteAll();
        // 알림: 이력 → 설정
        notificationHistoryJpaRepository.deleteAll();
        notificationJpaRepository.deleteAll();
        // 리뷰/찜/예약
        reviewJpaRepository.deleteAll();
        favoriteJpaRepository.deleteAll();
        reservationJpaRepository.deleteAll();
        // 파일 (엔티티 참조 FK 없음, 하지만 ref_id 로 연결되므로 부모보다 먼저)
        fileJpaRepository.deleteAll();
        // 가게 스케줄 / 영업시간 / 카운트 → 가게
        storeScheduleJpaRepository.deleteAll();
        businessHoursJpaRepository.deleteAll();
        storeCountJpaRepository.deleteAll();
        storeJpaRepository.deleteAll();
        // 태그/카테고리
        tagJpaRepository.deleteAll();
        categoryJpaRepository.deleteAll();
        // 사용자 계층: 아동 → 사업자 → OAuth → User → 레벨
        childJpaRepository.deleteAll();
        ownerJpaRepository.deleteAll();
        userOAuthJpaRepository.deleteAll();
        userJpaRepository.deleteAll();
        levelJpaRepository.deleteAll();
        // 독립 엔티티
        emailAuthenticationJpaRepository.deleteAll();
        outboxEventJpaRepository.deleteAll();
    }

    /**
     * 테스트용 User 생성 (아동용)
     */
    public User createTestUserForChild(String loginId, String name, String phone) {
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
     * 테스트용 User 생성 (사업자용)
     */
    public User createTestUserForOwner(String loginId, String name, String phone) {
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
     * 테스트용 Level 생성 (사업자용)
     */
    public Level createTestLevelForOwner(String name) {
        return levelJpaRepository.save(
                Level.builder()
                        .name(name)
                        .build()
        );
    }

    /**
     * 테스트용 Owner 생성 (Level, User와 함께 생성)
     */
    public Owner createTestOwner(String loginId, String name, String phone, String levelName, String businessNumber) {
        User user = createTestUserForOwner(loginId, name, phone);
        Level level = createTestLevelForOwner(levelName);

        return ownerJpaRepository.save(
                Owner.builder()
                        .user(user)
                        .level(level)
                        .businessNumber(businessNumber)
                        .build()
        );
    }

    /**
     * 테스트용 Owner 생성 (기본값 사용)
     */
    public Owner createTestOwner(String loginId, String name, String phone, String levelName) {
        return createTestOwner(loginId, name, phone, levelName, "1234567890");
    }

    /**
     * 테스트용 Child 생성 (User와 함께 생성)
     */
    public Child createTestChild(String loginId, String name, String phone, Boolean isVerified) {
        User user = createTestUserForChild(loginId, name, phone);
        return childJpaRepository.save(
                Child.builder()
                        .user(user)
                        .isVerified(isVerified != null ? isVerified : true)
                        .build()
        );
    }

    /**
     * 테스트용 Child 생성 (기본값 사용)
     */
    public Child createTestChild(String loginId, String name, String phone) {
        return createTestChild(loginId, name, phone);
    }

    /**
     * 테스트용 Category 생성
     */
    public Category createTestCategory(String name) {
        return categoryJpaRepository.save(Category.builder().name(name).build());
    }
}
