package com.backend.onharu.application;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.db.notification.NotificationHistoryJpaRepository;
import com.backend.onharu.infra.db.notification.NotificationJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;
import com.backend.onharu.domain.common.TestDataHelper;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("NotificationFacade 통합 테스트")
class NotificationFacadeTest {

    @Autowired
    private NotificationFacade notificationFacade;

    @Autowired


    private TestDataHelper testDataHelper;


    @Autowired
    private NotificationHistoryJpaRepository notificationHistoryJpaRepository;

    @Autowired
    private NotificationJpaRepository notificationJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @BeforeEach
    void setUp() {

        testDataHelper.cleanAll();

    }

    private User createTestUser(String loginIdSuffix) {
        return userJpaRepository.save(
                User.builder()
                        .loginId("test_user_" + loginIdSuffix + "_" + UUID.randomUUID())
                        .password("password123")
                        .name("테스트유저")
                        .phone("01012345678")
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build()
        );
    }

    private NotificationHistory createUnreadHistory(User user, String title) {
        return notificationHistoryJpaRepository.save(
                NotificationHistory.builder()
                        .user(user)
                        .type(NotificationHistoryType.RESERVATION_CREATED)
                        .title(title)
                        .message("알림 메시지")
                        .relatedEntityType("RESERVATION")
                        .relatedEntityId(1L)
                        .isRead(false)
                        .build()
        );
    }

    @Nested
    @DisplayName("알림 전체 읽음 처리 테스트")
    class MarkAllNotificationAsReadTest {

        @Test
        @DisplayName("성공: unpaged 조회 시 사용자의 모든 알림이 읽음 처리된다")
        void shouldMarkAllNotificationAsReadWithUnpaged() {
            // given
            User targetUser = createTestUser("target");
            User otherUser = createTestUser("other");

            createUnreadHistory(targetUser, "타겟 알림 1");
            createUnreadHistory(targetUser, "타겟 알림 2");
            createUnreadHistory(targetUser, "타겟 알림 3");
            NotificationHistory otherHistory = createUnreadHistory(otherUser, "다른 사용자 알림");

            // when
            notificationFacade.markAllNotificationAsRead(targetUser.getId());

            // then
            Page<NotificationHistory> targetHistories = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(targetUser.getId(), Pageable.unpaged());

            assertThat(targetHistories.getTotalElements()).isEqualTo(3);
            assertThat(targetHistories.getContent())
                    .extracting(NotificationHistory::getIsRead)
                    .containsOnly(true);

            NotificationHistory reloadedOtherHistory = notificationHistoryJpaRepository
                    .findById(otherHistory.getId())
                    .orElseThrow();
            assertThat(reloadedOtherHistory.getIsRead()).isFalse();
        }

        @Test
        @DisplayName("성공: 데이터가 많아도 unpaged 조회로 전체 알림을 읽음 처리한다")
        void shouldMarkAllHistoriesWhenUnpagedWithManyRows() {
            // given
            User targetUser = createTestUser("many");
            int historyCount = 25;
            for (int i = 1; i <= historyCount; i++) {
                createUnreadHistory(targetUser, "대량 알림 " + i);
            }

            // when
            notificationFacade.markAllNotificationAsRead(targetUser.getId());

            // then
            Page<NotificationHistory> targetHistories = notificationHistoryJpaRepository
                    .findByUser_IdOrderByCreatedAtDesc(targetUser.getId(), Pageable.unpaged());

            assertThat(targetHistories.getTotalElements()).isEqualTo(historyCount);
            assertThat(targetHistories.getContent()).allMatch(NotificationHistory::getIsRead);
        }
    }
}
