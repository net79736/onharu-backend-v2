package com.backend.onharu.domain.notification.dto;

public class NotificationCommand {
    /**
     * 알림 생성 커맨드
     */
    public record CreateNotificationCommand(
            Long userId,
            Boolean isSystemEnabled
    ) {
    }
}
