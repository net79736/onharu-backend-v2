package com.backend.onharu.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
@DisplayName("NotificationHistoryQueryService 통합 테스트")
class NotificationHistoryQueryServiceTest {

    @Autowired
    private NotificationHistoryQueryService notificationHistoryQueryService;

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

    private User createTestUser(String suffix) {
        return userJpaRepository.save(
                User.builder()
                        .loginId("notification_user_" + suffix + "_" + UUID.randomUUID())
                        .password("password123")
                        .name("테스트 유저")
                        .phone("01012345678")
                        .userType(UserType.CHILD)
                        .statusType(StatusType.ACTIVE)
                        .providerType(ProviderType.LOCAL)
                        .build()
        );
    }

    private NotificationHistory createHistory(User user, String title, boolean isRead) {
        return notificationHistoryJpaRepository.save(
                NotificationHistory.builder()
                        .user(user)
                        .type(NotificationHistoryType.RESERVATION_CREATED)
                        .title(title)
                        .message("테스트 알림 메시지")
                        .relatedEntityType("RESERVATION")
                        .relatedEntityId(10L)
                        .isRead(isRead)
                        .build()
        );
    }

    @Nested
    @DisplayName("읽지 않은 알림 조회 테스트")
    class FindUnreadNotificationsTest {

        @Test
        @DisplayName("성공: 특정 사용자 기준으로 읽지 않은 알림만 조회한다")
        void shouldFindOnlyUnreadNotificationsByUserId() {
            // given
            User targetUser = createTestUser("target");
            User otherUser = createTestUser("other");

            NotificationHistory unread1 = createHistory(targetUser, "읽지 않음 1", false);
            NotificationHistory unread2 = createHistory(targetUser, "읽지 않음 2", false);
            createHistory(targetUser, "이미 읽음", true);
            createHistory(otherUser, "다른 사용자 읽지 않음", false);

            // when
            List<NotificationHistory> unreadHistories = notificationHistoryQueryService
                    .findUnReadedNotificationHistoriesByUserId(targetUser.getId());

            // then
            assertThat(unreadHistories).hasSize(2);
            assertThat(unreadHistories)
                    .extracting(NotificationHistory::getId)
                    .contains(unread1.getId(), unread2.getId());
            assertThat(unreadHistories)
                    .allMatch(history -> history.getUser().getId().equals(targetUser.getId()));
            assertThat(unreadHistories)
                    .allMatch(history -> Boolean.FALSE.equals(history.getIsRead()));
        }

        @Test
        @DisplayName("성공: 읽지 않은 알림이 없으면 빈 목록을 반환한다")
        void shouldReturnEmptyWhenNoUnreadNotifications() {
            // given
            User targetUser = createTestUser("no_unread");
            createHistory(targetUser, "읽은 알림", true);

            // when
            List<NotificationHistory> unreadHistories = notificationHistoryQueryService
                    .findUnReadedNotificationHistoriesByUserId(targetUser.getId());

            // then
            assertThat(unreadHistories).isEmpty();
        }
    }
}
