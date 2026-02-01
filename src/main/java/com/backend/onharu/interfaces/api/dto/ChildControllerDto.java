package com.backend.onharu.interfaces.api.dto;

import java.util.List;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.reservation.model.Reservation;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChildControllerDto {

    public record GetChildProfileResponse(
            ChildProfileResponse child
    ) {
    }


    public record ChildProfileResponse(
            @Schema(description = "아이 ID", example = "1")
            Long id,

            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "이름", example = "김길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "계정 상태", example = "ACTIVE", allowableValues = {"PENDING", "ACTIVE", "LOCKED", "DELETED", "BLOCKED"})
            StatusType statusType,

            @Schema(description = "증명서 파일 경로", example = "/certificates/certificate.pdf")
            String certificate,

            @Schema(description = "지원 대상 승인 여부", example = "true")
            Boolean isVerified
    ) {
    }

    // 카드 관련 DTO는 SQL 스키마에 카드 테이블이 없으므로 주석 처리
    // 필요시 별도 카드 테이블 추가 후 활성화
    public record IssueCardRequest(
            @Schema(description = "카드 번호", example = "1234-5678-9012-3456")
            String cardNumber
    ) {
    }

    public record IssueCardResponse(
            @Schema(description = "카드 ID", example = "1")
            Long cardId
    ) {
    }

    public record UpdateCardRequest(
            @Schema(description = "카드 번호", example = "1234-5678-9012-3456")
            String cardNumber
    ) {
    }

    public record GetCardResponse(
            CardResponse card
    ) {
    }

    public record CardResponse(
            @Schema(description = "카드 ID", example = "1")
            Long id,

            @Schema(description = "카드 번호", example = "1234-5678-9012-3456")
            String cardNumber
    ) {
    }

    public record UpdateCertificateRequest(
            @Schema(description = "닉네임", example = "온하루친구")
            String nickname
    ) {
    }

    public record GetCertificateResponse(
            CertificateResponse certificate
    ) {
    }

    public record CertificateResponse(
            @Schema(description = "증명서 ID", example = "1")
            Long id,

            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "아동 이름", example = "김길동")
            String name,

            @Schema(description = "증명서 파일 경로", example = "/certificates/certificate.pdf")
            String certificate,

            @Schema(description = "지원 대상 승인 여부", example = "true")
            Boolean isVerified
    ) {
    }

    // 예약 관련 DTO
    public record ReservationResponse(
            @Schema(description = "예약 ID", example = "1")
            Long id,

            @Schema(description = "아이 ID", example = "1")
            Long childId,

            @Schema(description = "가게 일정 ID", example = "1")
            Long storeScheduleId,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "가게 이름", example = "따뜻한 식당")
            String storeName,

            @Schema(description = "예약 일정 날짜", example = "2024-12-31")
            java.time.LocalDate scheduleDate,

            @Schema(description = "예약 시작 시간", example = "14:00")
            java.time.LocalTime startTime,

            @Schema(description = "예약 종료 시간", example = "15:00")
            java.time.LocalTime endTime,

            @Schema(description = "인원 수", example = "1")
            Integer people,

            @Schema(description = "예약 상태", example = "WAITING", allowableValues = {"WAITING", "CANCELED", "COMPLETED"})
            String status,

            @Schema(description = "예약 시간", example = "2024-12-31T14:00:00")
            java.time.LocalDateTime reservationAt,

            @Schema(description = "취소 사유", example = "일정 변경으로 인한 취소")
            String cancelReason
    ) {
        public ReservationResponse(Reservation reservation) {
            this(
                reservation.getId(),
                reservation.getChild().getId(),
                reservation.getStoreSchedule().getId(),
                reservation.getStoreSchedule().getStore().getId(),
                reservation.getStoreSchedule().getStore().getName(),
                reservation.getStoreSchedule().getScheduleDate(),
                reservation.getStoreSchedule().getStartTime(),
                reservation.getStoreSchedule().getEndTime(),
                reservation.getPeople(),
                reservation.getStatus().name(),
                reservation.getReservationAt(),
                reservation.getCancelReason()
            );
        }
    }

    @Schema(description = "가게 예약 생성 요청")
    public record BookStoreRequest(
            @Schema(description = "가게 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long storeId,

            @Schema(description = "가게 일정 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long storeScheduleId,

            @Schema(description = "인원 수", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer people
    ) {
    }

    public record BookStoreResponse(
            ReservationResponse reservation
    ) {
    }

    public record GetMyBookingListResponse(
            List<ReservationResponse> reservations
    ) {
    }

    public record GetMyBookingDetailResponse(
            ReservationResponse reservation
    ) {
    }
}
