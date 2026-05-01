package com.backend.onharu.domain.notification.dto;

public class NotificationQuery {
    /**
     * 사용자 ID로 알림 조회 Query
     */
    public record GetNotificationByUserIdQuery(
            Long userId
    ) {
    }
}
