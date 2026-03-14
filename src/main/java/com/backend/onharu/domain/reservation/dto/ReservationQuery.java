package com.backend.onharu.domain.reservation.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import com.backend.onharu.domain.common.enums.ReservationType;

public class ReservationQuery {
    /**
     * 예약 ID로 단건 조회
     */
    public record GetReservationByIdQuery(
            Long reservationId
    ) {
    }

    /**
     * 아동 ID와 상태 필터로 예약 목록 조회 (내 예약 목록)
     */
    public record FindByChildIdAndStatusFilterQuery(
            Long childId,
            List<ReservationType> statusFilters
    ) {
    }

    /**
     * 가게 일정 ID로 예약 목록 조회
     */
    public record GetByStoreScheduleIdQuery(
            Long storeScheduleId
    ) {
    }

    /**
     * 가게 ID로 예약 목록 조회 (사업자용)
     */
    public record FindByStoreIdQuery(
            Long storeId
    ) {
    }

    /**
     * 가게 ID와 상태 필터로 예약 목록 조회 (사업자 예약 관리 목록)
     */
    public record FindByStoreIdAndStatusFilterQuery(
            Long storeId,
            Optional<ReservationType> statusFilter
    ) {
    }

    /**
     * 사업자 ID와 상태 필터로 예약 목록 조회
     * statusFilters가 비어 있으면 전체 조회 (ALL)
     */
    public record FindByOwnerIdAndStatusFilterQuery(
        Long ownerId,
        List<ReservationType> statusFilters
    ) {
    }

    /**
     * 예약 상태로 예약 목록 조회
     */
    public record FindAllByStatusQuery(
            ReservationType status
    ) {
    }

    /**
     * 아동 ID와 상태로 예약 목록 조회
     */
    public record FindByChildIdAndStatusQuery(
            Long childId,
            ReservationType status
    ) {
    }

    /**
     * 가게 ID와 상태로 예약 목록 조회
     */
    public record FindAllByStoreIdAndStatusQuery(
            Long storeId,
            ReservationType status
    ) {
    }

    /**
     * 아동 ID 기준 리뷰 미작성 COMPLETED 예약 조회
     */
    public record FindCompletedWithoutReviewByChildIdQuery(
            Long childId
    ) {
    }

    /**
     * 아동 ID 기준 다가오는 예약 조회 (scheduleDate >= fromDate)
     */
    public record FindUpcomingByChildIdQuery(
            Long childId,
            List<ReservationType> statusFilters,
            LocalDate fromDate
    ) {
    }

    /**
     * 사업자 ID 기준 다가오는 예약 조회 (scheduleDate >= fromDate)
     */
    public record FindUpcomingByOwnerIdQuery(
            Long ownerId,
            List<ReservationType> statusFilters,
            LocalDate fromDate
    ) {
    }
}
