package com.backend.onharu.application;

import java.util.List;

import org.springframework.stereotype.Component;

import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CancelReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetByStoreScheduleIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationCommandService;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

/**
 * 결식 아동 Facade
 */
@Component
@RequiredArgsConstructor
public class ChildFacade {

    private final ChildQueryService childQueryService;
    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;
    private final StoreScheduleQueryService storeScheduleQueryService;

    /**
     * 예약 하기
     */
    public void reserve(CreateReservationCommand command) {
        // 결식 아동 조회   
        Child child = childQueryService.getChildById(new GetChildByIdQuery(command.childId()));
        
        // 가게 일정 조회
        StoreSchedule storeSchedule = storeScheduleQueryService.getStoreScheduleById(new GetStoreScheduleByIdQuery(command.storeScheduleId()));

        // 조회한 가게 일정이 이미 예약된 일정인지 체크 (테이블 조회해서 확인)
        Reservation reservation = reservationQueryService.getByStoreScheduleId(new GetByStoreScheduleIdQuery(command.storeScheduleId()));        
        if (reservation != null) {
            throw new CoreException(ErrorType.Reservation.RESERVATION_ALREADY_EXISTS);
        }

        // 예약 생성
        reservationCommandService.createReservation(command, storeSchedule, child);
    }

    /**
     * 예약 취소
     */
    public void cancelReservation(CancelReservationCommand command, Long childId) {
        // 예약 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(command.reservationId()));

        // 현재 로그인한 아동 정보 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 예약이 해당 아동에 속하는지 확인
        reservation.BelongsTo(child.getId());

        // 예약 취소
        reservationCommandService.cancelReservation(command);
    }

    /**
     * 내가 신청한 예약 목록 조회
     * 
     * @return 내가 신청한 예약 목록
     */
    public List<Reservation> getMyBookings(Long childId) {
        // 현재 로그인한 아동 정보 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 내가 신청한 예약 목록 조회
        return reservationQueryService.findByChildId(new FindByChildIdQuery(child.getId()));
    }

    /**
     * 내가 신청한 특정 예약의 상세 정보를 조회
     * @param reservationId 예약 ID
     * @return 내가 신청한 특정 예약의 상세 정보
     */
    public Reservation getMyBooking(Long reservationId, Long childId) {
        // 현재 로그인한 아동 정보 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(childId));

        // 내가 신청한 특정 예약의 상세 정보 조회
        Reservation reservation = reservationQueryService.getReservation(new GetReservationByIdQuery(reservationId));

        // 현재 로그인한 아동 정보와 예약자 정보가 다르면 예외 발생
        reservation.BelongsTo(child.getId());

        return reservation;
    }
}
