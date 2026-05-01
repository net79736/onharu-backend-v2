package com.backend.onharu.domain.notification.dto;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

/**
 * 알림 Repository 파라미터
 */
public class NotificationRepositoryParam {
    /**
     * 사용자 ID로 알림 조회용 파라미터
     */
    public record GetNotificationByUserIdParam(
            Long userId
    ) {
        public GetNotificationByUserIdParam {
            if (userId == null) {
                throw new CoreException(ErrorType.Notification.NOTIFICATION_USER_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}
