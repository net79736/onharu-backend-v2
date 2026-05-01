package com.backend.onharu.domain.reservation.dto;

import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.common.enums.UserType;

public class ReservationCommand {
    /**
     * 예약 생성 커맨드
     */
    public record CreateReservationCommand(
            Long childId,
            Long storeScheduleId,
            Integer people
    ) {
    }

    /**
     * 예약 취소 커맨드
     */
    public record CancelReservationCommand(
            Long reservationId,
            UserType cancelRequestedBy,
            String cancelReason
    ) {
    }

    /**
     * 예약 확정 커맨드 (WAITING → CONFIRMED)
     */
    public record ConfirmReservationCommand(
            Long reservationId
    ) {
    }

    /**
     * 예약 완료 처리 커맨드 (CONFIRMED → COMPLETED)
     */
    public record CompleteReservationCommand(
            Long reservationId
    ) {
    }

    /**
     * 예약 상태 변경 커맨드
     */
    public record ChangeReservationStatusCommand(
            Long reservationId,
            ReservationType status
    ) {
    }
}
