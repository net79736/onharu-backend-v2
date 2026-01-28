package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.backend.onharu.domain.common.enums.StatusType;

import io.swagger.v3.oas.annotations.media.Schema;

public class OwnerControllerDto {

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

    // 예약 관련 DTO
    public record GetStoreBookingListResponse(
            java.util.List<ReservationResponse> reservations
    ) {
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

            @Schema(description = "예약 가능 일정 ID", example = "1")
            Long availableScheduleId,

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
            java.time.LocalDateTime reservationAt,

            @Schema(description = "취소 사유", example = "일정 변경으로 인한 취소")
            String cancelReason
    ) {
    }

    public record SetAvailableDatesRequest(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "예약 가능한 일정 목록")
            List<AvailableScheduleRequest> availableSchedules
    ) {
    }

    public record AvailableScheduleRequest(
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

    public record RemoveAvailableDatesRequest(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "삭제할 일정 ID 목록")
            List<Long> availableScheduleIds
    ) {
    }

    public record GetAvailableDatesResponse(
            @Schema(description = "예약 가능한 일정 목록")
            List<AvailableScheduleResponse> availableSchedules
    ) {
    }

    public record AvailableScheduleResponse(
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
            @Schema(description = "수정할 일정 목록")
            List<UpdateAvailableScheduleRequest> availableSchedules
    ) {
    }

    public record UpdateAvailableScheduleRequest(
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
}
