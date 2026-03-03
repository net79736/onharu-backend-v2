package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.StoreResponse;
import com.fasterxml.jackson.annotation.JsonFormat;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class OwnerControllerDto {

    public record GetMyStoresRequest(
            @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
            Integer pageNum,

            @Schema(description = "페이지당 항목 수", example = "10")
            Integer perPage,

            @Schema(description = "정렬 기준", example = "id", allowableValues = {"id", "name", "favoriteCount"})
            String sortField,

            @Schema(description = "정렬 방향", example = "desc", allowableValues = {"asc", "desc"})
            String sortDirection
    ) {
    }

    public record CreateOwnerRequest(
            @Schema(description = "가게 사장 사용자 ID", example = "child123")
            String userId,

            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    public record CreateOwnerResponse(
            @Schema(description = "ID", example = "1")
            Long id,

            @Schema(description = "가게 사장 사용자 ID", example = "child123")
            String userId
    ) {
    }

    public record UpdateOwnerRequest(
            @Schema(description = "레벨 ID", example = "1")
            String levelId,

            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    public record GetOwnerResponse(
            OwnerResponse owner
    ) {
    }

    public record OwnerResponse(
            @Schema(description = "사업자 ID", example = "1")
            Long id,

            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "이름", example = "김길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "계정 상태", example = "ACTIVE", allowableValues = {"PENDING", "ACTIVE", "LOCKED", "DELETED", "BLOCKED"})
            StatusType statusType,

            @Schema(description = "레벨 ID", example = "1")
            String levelId,

            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    // 사업자 가게 목록 조회 DTO
    public record GetMyStoresResponse(
            @Schema(description = "가게 목록")
            List<StoreResponse> stores,

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

    // 예약 관련 DTO
    @Schema(description = "예약 관리 목록 조회 요청")
    public record GetStoreBookingsRequest(
            @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
            Integer pageNum,

            @Schema(description = "페이지당 항목 수", example = "10")
            Integer perPage,

            @Schema(description = "예약 상태 필터 (ALL: 전체, 그 외: 해당 상태만)", example = "ALL", allowableValues = {"ALL", "WAITING", "CONFIRMED", "CANCELED", "COMPLETED", "REJECTED"})
            ReservationStatusFilter statusFilter,

            @Schema(description = "정렬 기준", example = "id", allowableValues = {"id"})
            String sortField,

            @Schema(description = "정렬 방향", example = "desc", allowableValues = {"asc", "desc"})
            String sortDirection
    ) {
        public Integer pageNum() {
            return pageNum != null && pageNum > 0 ? pageNum : 1;
        }

        public Integer perPage() {
            return perPage != null && perPage > 0 ? perPage : 10;
        }

        public ReservationStatusFilter effectiveStatusFilter() {
            return statusFilter != null ? statusFilter : ReservationStatusFilter.ALL;
        }
    }

    public record GetStoreBookingListResponse(
            List<ReservationResponse> reservations,

            @Schema(description = "전체 예약 개수 (페이징 사용 시)")
            Long totalCount,

            @Schema(description = "현재 페이지 번호 (페이징 사용 시)")
            Integer currentPage,

            @Schema(description = "전체 페이지 수 (페이징 사용 시)")
            Integer totalPages,

            @Schema(description = "페이지당 항목 수 (페이징 사용 시)")
            Integer perPage
    ) {
        public static GetStoreBookingListResponse of(List<ReservationResponse> reservations, long totalCount, int currentPage, int totalPages, int perPage) {
            return new GetStoreBookingListResponse(reservations, totalCount, currentPage, totalPages, perPage);
        }
    }

    public record GetStoreBookingDetailResponse(
            ReservationResponse reservation
    ) {
    }

    public record ReservationResponse(
            @Schema(description = "예약 ID", example = "1")
            Long id,

            @Schema(description = "아이 ID", example = "1")
            Long childId,

            @Schema(description = "아이 닉네임", example = "코끼리땃쥐")
            String childNickname,

            @Schema(description = "가게 일정 ID", example = "1")
            Long storeScheduleId,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "가게 이름", example = "따뜻한 식당")
            String storeName,

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
            String cancelReason
    ) {
        public ReservationResponse(Reservation reservation) {
            this(
                reservation.getId(),
                reservation.getChild().getId(),
                reservation.getChild().getNickname(),
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

    public record SetAvailableDatesRequest(
            @Valid
            @NotEmpty(message = "예약 가능한 일정은 최소 1개 이상 등록해야 합니다.")
            @Schema(description = "예약 가능한 일정 목록", requiredMode = Schema.RequiredMode.REQUIRED)
            List<StoreScheduleRequest> storeSchedules
    ) {
    }

    public record StoreScheduleRequest(
            @NotNull(message = "일정 날짜는 필수입니다.")
            @FutureOrPresent(message = "일정 날짜는 오늘 이후여야 합니다.")
            @Schema(description = "일정 날짜", example = "2024-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDate scheduleDate,

            @NotNull(message = "시작 시간은 필수입니다.")
            @JsonFormat(pattern = "HH:mm")
            @Schema(description = "시작 시간 (HH:mm 형식)", example = "14:00", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalTime startTime,

            @NotNull(message = "종료 시간은 필수입니다.")
            @JsonFormat(pattern = "HH:mm")
            @Schema(description = "종료 시간 (HH:mm 형식)", example = "15:00", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalTime endTime,

            @NotNull(message = "최대 인원은 필수입니다.")
            @Positive(message = "최대 인원은 1 이상이어야 합니다.")
            @Schema(description = "최대 인원", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer maxPeople
    ) {
    }

    public record RemoveAvailableDatesRequest(
            @Schema(description = "삭제할 일정 ID 목록")
            List<Long> storeScheduleIds
    ) {
    }

    public record GetAvailableDatesResponse(
            @Schema(description = "예약 가능한 일정 목록")
            List<StoreScheduleResponse> storeSchedules
    ) {
    }

    public record StoreScheduleResponse(
            @Schema(description = "일정 ID", example = "1")
            Long id,

            @Schema(description = "일정 날짜", example = "2024-12-31")
            LocalDate scheduleDate,

            @Schema(description = "시작 시간", example = "14:00")
            LocalTime startTime,

            @Schema(description = "종료 시간", example = "15:00")
            LocalTime endTime,

            @Schema(description = "최대 인원", example = "10")
            Integer maxPeople
    ) {
    }

    public record UpdateAvailableDatesRequest(
            @Valid
            @NotEmpty(message = "수정할 일정은 최소 1개 이상이어야 합니다.")
            @Schema(description = "수정할 일정 목록", requiredMode = Schema.RequiredMode.REQUIRED)
            List<UpdateStoreScheduleRequest> storeSchedules
    ) {
    }

    public record UpdateStoreScheduleRequest(
            @NotNull(message = "일정 ID는 필수입니다.")
            @Schema(description = "일정 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long id,

            @NotNull(message = "일정 날짜는 필수입니다.")
            @FutureOrPresent(message = "일정 날짜는 오늘 이후여야 합니다.")
            @Schema(description = "일정 날짜", example = "2024-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDate scheduleDate,

            @NotNull(message = "시작 시간은 필수입니다.")
            @JsonFormat(pattern = "HH:mm")
            @Schema(description = "시작 시간 (HH:mm 형식)", example = "14:00", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalTime startTime,

            @NotNull(message = "종료 시간은 필수입니다.")
            @JsonFormat(pattern = "HH:mm")
            @Schema(description = "종료 시간 (HH:mm 형식)", example = "15:00", type = "string", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalTime endTime,

            @NotNull(message = "최대 인원은 필수입니다.")
            @Positive(message = "최대 인원은 1 이상이어야 합니다.")
            @Schema(description = "최대 인원", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
            Integer maxPeople
    ) {
    }

    @Schema(description = "예약 취소 요청")
    public record CancelReservationRequest(
        @Size(max = 30, message = "예약 취소 사유는 30자 이하여야 합니다.")
        @Schema(description = "예약 취소 사유", example = "일정 변경으로 인한 취소")
        String cancelReason
    ) {
    }
}
