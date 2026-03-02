package com.backend.onharu.domain.reservation.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.ChangeReservationStatusCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CompleteReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.ConfirmReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.RejectReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStatusAndScheduleDateBeforeThanParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetReservationByIdParam;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.repository.ReservationRepository;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ReservationCommandService {
    private final ReservationRepository reservationRepository;

    /**
     * 예약 생성
     * 
     * 주의: Child 엔티티는 별도로 조회해서 전달해야 합니다.
     */
    public Reservation createReservation(CreateReservationCommand command, StoreSchedule storeSchedule, Child child) {
        Reservation reservation = Reservation.builder()
                .child(child)
                .storeSchedule(storeSchedule)
                .people(command.people())
                .reservationAt(LocalDateTime.now())
                .status(ReservationType.WAITING)
                .build();

        return reservationRepository.save(reservation);
    }

    /**
     * 예약 취소
     */
    public void cancelReservation(CancelReservationCommand command) {
        Reservation reservation = reservationRepository.getReservation(
                new GetReservationByIdParam(command.reservationId()));
        
        reservation.cancel(command.cancelReason());
    }

    /**
     * 예약 거절
     */
    public void rejectReservation(RejectReservationCommand command) {
        Reservation reservation = reservationRepository.getReservation(
                new GetReservationByIdParam(command.reservationId()));
        
        reservation.reject(command.rejectReason());
    }

    /**
     * 예약 확정 (WAITING → CONFIRMED)
     */
    public void confirmReservation(ConfirmReservationCommand command) {
        Reservation reservation = reservationRepository.getReservation(
                new GetReservationByIdParam(command.reservationId()));

        reservation.confirm(); // 예약 확정 처리
    }

    /**
     * 예약 완료 처리 (CONFIRMED → COMPLETED)
     */
    public void completeReservation(CompleteReservationCommand command) {
        Reservation reservation = reservationRepository.getReservation(
                new GetReservationByIdParam(command.reservationId()));
        
        reservation.complete(); // 예약 완료 처리
    }

    /**
     * 예약 상태 변경
     */
    public void changeReservationStatus(ChangeReservationStatusCommand command) {
        Reservation reservation = reservationRepository.getReservation(
                new GetReservationByIdParam(command.reservationId()));
        
        reservation.changeStatus(command.status());
    }

    /**
     * 만료된 예약을 처리합니다. (WAITING → CANCELED)
     */
    public void expireOverDueReservations() {
        // 대기중 예약 중 예약 시간이 지난 예약을 조회
        List<Reservation> reservations = reservationRepository.findByStatusAndScheduleDateBeforeThan(new FindByStatusAndScheduleDateBeforeThanParam(ReservationType.WAITING, LocalDate.now()));

        // 대기중 예약 중 예약 시간이 지난 예약을 처리
        for (Reservation reservation : reservations) {
            log.info("만료된 예약 처리: " + reservation.getId());
            log.info("예약 시간: " + reservation.getReservationAt());
            log.info("현재 시간: " + LocalDateTime.now());
            reservation.expire();
            reservationRepository.save(reservation);
        }
    }
}
