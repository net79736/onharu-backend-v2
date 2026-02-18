package com.backend.onharu.domain.reservation.dto;

import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.interfaces.api.dto.ReservationStatusFilter;

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
            ReservationStatusFilter statusFilter
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
}
