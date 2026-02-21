package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.notification.model.Notification;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public class NotificationControllerDto {
    @Schema(description = "알림 조회 응답")
    public record GetNotificationResponse(
        NotificationResponse notificationResponse
    ) {
    }

    @Schema(description = "알림 수정 요청")
    public record UpdateNotificationRequest(
        @NotNull(message = "시스템 알림 수신 여부는 필수입니다.")
        @Schema(description = "시스템 알림 수신 여부", example = "true", allowableValues = {"true", "false"})
        Boolean isSystemEnabled
    ) {
    }

    @Schema(description = "알림 수정 응답")
    public record UpdateNotificationResponse(
        NotificationResponse notificationResponse
    ) {
    }

    public record NotificationResponse(
        @Schema(description = "사용자 로그인 ID")
        String loginId,

        @Schema(description = "시스템 알림 수신 여부")
        Boolean isSystemEnabled
    ) {
        public NotificationResponse(Notification notification) {
            this(
                notification.getUser().getLoginId(),
                notification.getIsSystemEnabled()
            );
        }
    }
}
