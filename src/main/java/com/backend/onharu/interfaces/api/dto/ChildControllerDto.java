package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.reservation.model.Reservation;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

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

    @Schema(description = "내가 신청한 예약 목록 조회 요청")
    public record GetMyBookingsRequest(
            @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
            Integer pageNum,

            @Schema(description = "페이지당 항목 수", example = "10")
            Integer perPage,

            @Schema(description = "예약 상태 필터 (ALL: 전체, 그 외: 해당 상태만)", example = "ALL", allowableValues = {"ALL", "WAITING", "CONFIRMED", "CANCELED", "COMPLETED"})
            ReservationStatusFilter statusFilter,

            @Schema(description = "정렬 기준", example = "id", allowableValues = {"id", "reservationAt", "scheduleDate"})
            String sortField,

            @Schema(description = "정렬 방향", example = "desc", allowableValues = {"asc", "desc"})
            String sortDirection
    ) {
        /**
         * 기본값 설정: pageNum이 null이거나 0 이하면 1, perPage가 null이거나 0 이하면 10
         */
        public Integer pageNum() {
            return pageNum != null && pageNum > 0 ? pageNum : 1;
        }

        public Integer perPage() {
            return perPage != null && perPage > 0 ? perPage : 10;
        }
    }
    
    // 예약 관련 DTO
    @Schema(name = "ChildReservationResponse", description = "아동 예약 응답")
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

            @Schema(description = "가게 주소", example = "서울시 강남구 테헤란로 123")
            String storeAddress,

            @Schema(description = "예약 일정 날짜", example = "2024-12-31")
            LocalDate scheduleDate,

            @Schema(description = "예약 시작 시간", example = "14:00")
            LocalTime startTime,

            @Schema(description = "예약 종료 시간", example = "15:00")
            LocalTime endTime,

            @Schema(description = "인원 수", example = "1")
            Integer people,

            @Schema(description = "예약 상태", example = "WAITING", allowableValues = {"WAITING", "CANCELED", "COMPLETED"})
            String status,

            @Schema(description = "예약 시간", example = "2024-12-31T14:00:00")
            LocalDateTime reservationAt,

            @Schema(description = "취소 사유", example = "일정 변경으로 인한 취소")
            String cancelReason,

            @Schema(description = "리뷰 작성 여부", example = "true")
            boolean reviewed,

            @Schema(description = "가게 주인의 사용자 ID", example = "10")
            Long userId
    ) {
        public ReservationResponse(Reservation reservation, boolean reviewed) {
            this(
                reservation.getId(),
                reservation.getChild().getId(),
                reservation.getStoreSchedule().getId(),
                reservation.getStoreSchedule().getStore().getId(),
                reservation.getStoreSchedule().getStore().getName(),
                reservation.getStoreSchedule().getStore().getAddress(),
                reservation.getStoreSchedule().getScheduleDate(),
                reservation.getStoreSchedule().getStartTime(),
                reservation.getStoreSchedule().getEndTime(),
                reservation.getPeople(),
                reservation.getStatus().name(),
                reservation.getReservationAt(),
                reservation.getCancelReason(),
                reviewed,
                reservation.getStoreSchedule().getStore().getOwner().getUser().getId()
            );
        }
    }

    @Schema(description = "가게 예약 생성 요청")
    public record BookStoreRequest(
            @Schema(description = "가게 일정 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long storeScheduleId,

            @Schema(description = "인원 수", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer people
    ) {
    }

    @Schema(description = "예약 취소 요청")
    public record CancelReservationRequest(
            @Size(max = 30, message = "예약 취소 사유는 30자 이하여야 합니다.")
            @Schema(description = "예약 취소 사유", example = "일정 변경으로 인한 취소")
            String cancelReason
    ) {
    }

    public record BookStoreResponse(
            ReservationResponse reservation
    ) {
    }

    public record GetMyBookingListResponse(
            @Schema(description = "예약 목록")
            List<ReservationResponse> reservations,

            @Schema(description = "전체 가게 개수")
            Long totalCount,
            
            @Schema(description = "현재 페이지 번호")
            Integer currentPage,
            
            @Schema(description = "전체 페이지 수")
            Integer totalPages,
            
            @Schema(description = "페이지당 항목 수")
            Integer perPage
    ) {
    }

    public record GetMyBookingDetailResponse(
        @Schema(description = "예약 목록")
        ReservationResponse reservation
    ) {
    }

    public record GetMyBookingSummaryResponse(
        @Schema(description = "다가오는 방문 예정 예약 목록 (최대 2건)")
        List<ReservationResponse> upcomingReservations,

        @Schema(description = "리뷰 작성 대상 예약 목록 (최대 2건, 리뷰 미작성 건만 포함)")
        List<ReservationResponse> reviewTargetReservations
    ) {
    }
}
