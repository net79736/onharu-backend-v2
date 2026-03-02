package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.notification.model.Notification;
import com.backend.onharu.domain.notification.model.NotificationHistory;

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

    // ---- 알림 내역(NotificationHistory) ----

    @Schema(description = "알림 내역 목록 조회 요청")
    public record GetNotificationHistoriesRequest(
        @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
        Integer pageNum,

        @Schema(description = "페이지당 항목 수", example = "10")
        Integer perPage,

        @Schema(description = "정렬 기준", example = "createdAt", allowableValues = {"id", "createdAt"})
        String sortField,

        @Schema(description = "정렬 방향", example = "desc", allowableValues = {"asc", "desc"})
        String sortDirection
    ) {
    }

    @Schema(description = "알림 내역 목록 조회 응답")
    public record GetNotificationHistoriesResponse(
        @Schema(description = "알림 내역 목록")
        List<NotificationHistoryResponse> histories,

        @Schema(description = "전체 개수")
        Long totalCount,

        @Schema(description = "현재 페이지 번호")
        Integer currentPage,

        @Schema(description = "전체 페이지 수")
        Integer totalPages,

        @Schema(description = "페이지당 항목 수")
        Integer perPage
    ) {
    }

    @Schema(description = "알림 읽음 처리 응답")
    public record MarkNotificationReadResponse(
        @Schema(description = "읽음 처리된 알림 내역")
        NotificationHistoryResponse notificationHistory
    ) {
    }

    @Schema(description = "알림 내역 한 건 응답")
    public record NotificationHistoryResponse(
        @Schema(description = "알림 내역 ID")
        Long id,

        @Schema(description = "알림 유형")
        NotificationHistoryType type,

        @Schema(description = "제목")
        String title,

        @Schema(description = "본문")
        String message,

        @Schema(description = "연관 엔티티 타입 (예: RESERVATION)")
        String relatedEntityType,

        @Schema(description = "연관 엔티티 ID")
        Long relatedEntityId,

        @Schema(description = "읽음 여부")
        Boolean isRead,

        @Schema(description = "생성 시각")
        LocalDateTime createdAt
    ) {
        public NotificationHistoryResponse(NotificationHistory entity) {
            this(
                entity.getId(),
                entity.getType(),
                entity.getTitle(),
                entity.getMessage(),
                entity.getRelatedEntityType(),
                entity.getRelatedEntityId(),
                entity.getIsRead(),
                entity.getCreatedAt()
            );
        }
    }
}
