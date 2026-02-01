package com.backend.onharu.domain.reservation.dto;

import com.backend.onharu.domain.common.enums.ReservationType;

public class ReservationRepositroyParam {
    /**
     * 예약 ID로 단건 조회용 파라미터
     */
    public record GetReservationByIdParam(
            Long reservationId
    ) {
    }

    /**
     * 아동 ID로 예약 목록 조회용 파라미터
     */
    public record FindAllByChildIdParam(
            Long childId
    ) {
    }

    /**
     * 가게 일정 ID로 예약 목록 조회용 파라미터
     */
    public record GetByStoreScheduleIdParam(
            Long storeScheduleId
    ) {
    }

    /**
     * 가게 ID로 예약 목록 조회용 파라미터
     */
    public record FindByStoreIdParam(
            Long storeId
    ) {
    }

    /**
     * 예약 상태로 예약 목록 조회용 파라미터
     */
    public record FindAllByStatusParam(
            ReservationType status
    ) {
    }

    /**
     * 아동 ID와 상태로 예약 목록 조회용 파라미터
     */
    public record FindByChildIdAndStatusParam(
            Long childId,
            ReservationType status
    ) {
    }

    /**
     * 가게 ID와 상태로 예약 목록 조회용 파라미터
     */
    public record FindByStoreIdAndStatusParam(
            Long storeId,
            ReservationType status
    ) {
    }
}
