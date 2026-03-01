package com.backend.onharu.domain.notification.dto;

import org.springframework.data.domain.Pageable;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

/**
 * 알림 히스토리 Repository 파라미터
 */
public class NotificationHistoryRepositoryParam {

    /**
     * 알림 히스토리 ID로 조회용 파라미터
     */
    public record GetNotificationHistoryByIdParam(Long id) {
        public GetNotificationHistoryByIdParam {
            if (id == null) {
                throw new CoreException(ErrorType.Notification.NOTIFICATION_HISTORY_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 사용자 ID로 알림 히스토리 목록 조회용 파라미터
     */
    public record FindByUserIdParam(Long userId, Pageable pageable) {
        public FindByUserIdParam {
            if (userId == null) {
                throw new CoreException(ErrorType.Notification.NOTIFICATION_HISTORY_USER_ID_MUST_NOT_BE_NULL);
            }
        }
    }
}
