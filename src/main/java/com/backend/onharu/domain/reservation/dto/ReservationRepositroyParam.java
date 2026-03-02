package com.backend.onharu.domain.reservation.dto;

import java.time.LocalDate;

import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.interfaces.api.dto.ReservationStatusFilter;

public class ReservationRepositroyParam {
    /**
     * 예약 ID로 단건 조회용 파라미터
     */
    public record GetReservationByIdParam(
            Long reservationId
    ) {
        public GetReservationByIdParam {
            if (reservationId == null) {
                throw new CoreException(ErrorType.Reservation.RESERVATION_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 아동 ID로 예약 목록 조회용 파라미터
     */
    public record FindByChildIdAndStatusFilterParam(
            Long childId,
            ReservationStatusFilter statusFilter
    ) {
        public FindByChildIdAndStatusFilterParam {
            if (childId == null) {
                throw new CoreException(ErrorType.Reservation.RESERVATION_CHILD_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 일정 ID로 예약 목록 조회용 파라미터
     */
    public record GetByStoreScheduleIdParam(
            Long storeScheduleId
    ) {
        public GetByStoreScheduleIdParam {
            if (storeScheduleId == null) {
                throw new CoreException(ErrorType.Reservation.RESERVATION_STORE_SCHEDULE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 ID로 예약 목록 조회용 파라미터
     */
    public record FindByStoreIdParam(
            Long storeId
    ) {
        public FindByStoreIdParam {
            if (storeId == null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 ID와 상태 필터로 예약 목록 조회용 파라미터
     */
    public record FindByStoreIdAndStatusFilterParam(
            Long storeId,
            ReservationStatusFilter statusFilter
    ) {
        public FindByStoreIdAndStatusFilterParam {
            if (storeId == null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 예약 상태로 예약 목록 조회용 파라미터
     */
    public record FindAllByStatusParam(
            ReservationType status
    ) {
        public FindAllByStatusParam {
            if (status == null) {
                throw new CoreException(ErrorType.Reservation.RESERVATION_STATUS_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 아동 ID와 상태로 예약 목록 조회용 파라미터
     */
    public record FindByChildIdAndStatusParam(
            Long childId,
            ReservationType status
    ) {
        public FindByChildIdAndStatusParam {
            if (childId == null) {
                throw new CoreException(ErrorType.Reservation.RESERVATION_CHILD_ID_MUST_NOT_BE_NULL);
            }
            if (status == null) {
                throw new CoreException(ErrorType.Reservation.RESERVATION_STATUS_MUST_NOT_BE_NULL);
            }
        }
    }

    public record FindByStatusAndScheduleDateBeforeThanParam(
            ReservationType status,
            LocalDate date
    ) {
        public FindByStatusAndScheduleDateBeforeThanParam {
            if (status == null) {
                throw new CoreException(ErrorType.Reservation.RESERVATION_STATUS_MUST_NOT_BE_NULL);
            }
            if (date == null) {
                throw new CoreException(ErrorType.Reservation.RESERVATION_DATE_MUST_NOT_BE_NULL);
            }
        }
    }
}
