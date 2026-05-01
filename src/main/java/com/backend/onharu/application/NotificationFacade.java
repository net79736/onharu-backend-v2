package com.backend.onharu.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.notification.dto.NotificationCommand.CreateNotificationCommand;
import com.backend.onharu.domain.notification.dto.NotificationQuery.GetNotificationByUserIdQuery;
import com.backend.onharu.domain.notification.model.Notification;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.domain.notification.service.NotificationCommandService;
import com.backend.onharu.domain.notification.service.NotificationHistoryCommandService;
import com.backend.onharu.domain.notification.service.NotificationHistoryQueryService;
import com.backend.onharu.domain.notification.service.NotificationQueryService;
import com.backend.onharu.domain.user.dto.UserQuery.GetUserByIdQuery;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.domain.user.service.UserQueryService;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.UpdateNotificationRequest;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class NotificationFacade {

    private final NotificationQueryService notificationQueryService;
    private final NotificationCommandService notificationCommandService;
    private final NotificationHistoryQueryService notificationHistoryQueryService;
    private final NotificationHistoryCommandService notificationHistoryCommandService;
    private final UserQueryService userQueryService;

    /**
     * 사용자 ID로 알림 조회
     * 
     * @param userId 사용자 ID
     * @return 알림
     */
    public Notification getNotification(Long userId) {
        return notificationQueryService.getNotificationByUserId(new GetNotificationByUserIdQuery(userId));
    }

    /**
     * 사용자에 대한 알림 설정이 없으면 생성합니다.
     * 로그인 시점 등에서 호출하여 알림 객체 존재를 보장합니다.
     *
     * @param userId 사용자 ID
     * @return 기존 또는 신규 생성된 Notification
     */
    @Transactional
    public Notification ensureNotificationExists(Long userId) {
        return notificationQueryService.findOptionalByUserId(new GetNotificationByUserIdQuery(userId))
            .orElseGet(() -> createNotification(userId, true));
    }

    /**
     * 사용자 ID로 알림 생성
     * 
     * @param userId 사용자 ID
     * @param isSystemEnabled 시스템 알림 수신 여부
     * @return 알림
     */
    @Transactional
    public Notification createNotification(Long userId, Boolean isSystemEnabled) {
        // 사용자 조회
        User user = userQueryService.getUser(new GetUserByIdQuery(userId));
        // 알림 생성
        return notificationCommandService.createNotification(new CreateNotificationCommand(userId, isSystemEnabled), user);
    }

    /**
     * 사용자 ID로 알림 수정
     * 
     * @param userId 사용자 ID
     * @param request 알림 수정 요청
     * @return 알림
     */
    @Transactional
    public Notification updateNotification(Long userId, UpdateNotificationRequest request) {
        // 사용자 조회 (존재 검증)
        userQueryService.getUser(new GetUserByIdQuery(userId));
        // 알림 조회
        Notification notification = notificationQueryService.getNotificationByUserId(new GetNotificationByUserIdQuery(userId));
        // 알림 수정
        notificationCommandService.updateNotification(notification, request.isSystemEnabled());
        return notification;
    }

    /**
     * 사용자 ID로 알림 내역 목록 조회 (페이징)
     * 로그인한 사용자(User)의 알림 내역을 조회합니다.
     *
     * @param userId   사용자 ID
     * @param pageable 페이징·정렬 정보
     * @return 알림 히스토리 페이지
     */
    public Page<NotificationHistory> getNotificationHistories(Long userId, Pageable pageable) {
        return notificationHistoryQueryService.findPageByUserId(userId, pageable);
    }

    /**
     * 알림 히스토리를 읽음 처리합니다.
     *
     * @param userId    요청한 사용자 ID
     * @param historyId 알림 히스토리 ID
     * @return 읽음 처리된 알림 히스토리
     */
    @Transactional
    public NotificationHistory markNotificationHistoryAsRead(Long userId, Long historyId) {
        return notificationHistoryCommandService.markAsRead(historyId, userId);
    }

    /**
     * 사용자 ID로 알림 히스토리 전체를 읽음 처리합니다.
     *
     * @param userId 사용자 ID
     */
    @Transactional
    public void markAllNotificationAsRead(Long userId) {
        notificationHistoryQueryService.findUnReadedNotificationHistoriesByUserId(userId).forEach(NotificationHistory::markAsRead);
    }
}
