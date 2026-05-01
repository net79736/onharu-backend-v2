package com.backend.onharu.domain.notification.repository;

import java.util.Optional;

import com.backend.onharu.domain.notification.dto.NotificationRepositoryParam.GetNotificationByUserIdParam;
import com.backend.onharu.domain.notification.model.Notification;

/**
 * 알림 Repository 인터페이스
 */
public interface NotificationRepository {
    /**
     * 알림 저장
     */
    Notification save(Notification notification);

    /**
     * 사용자 ID로 알림 조회 (없으면 예외)
     */
    Notification getNotificationByUserId(GetNotificationByUserIdParam param);

    /**
     * 사용자 ID로 알림 조회
     */
    Optional<Notification> findOptionalByUserId(GetNotificationByUserIdParam param);
}
