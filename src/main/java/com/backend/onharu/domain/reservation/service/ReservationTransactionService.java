package com.backend.onharu.domain.reservation.service;

import static com.backend.onharu.domain.common.enums.NotificationHistoryType.RESERVATION_CREATED;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_PEOPLE_EXCEEDS_MAX;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_PEOPLE_MUST_NOT_BE_NULL;

import java.time.LocalDateTime;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.dto.ChildQuery.GetChildByIdQuery;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.service.ChildQueryService;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetByStoreScheduleIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.event.model.ReservationEvent;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationTransactionService {

    private final ChildQueryService childQueryService;
    private final StoreScheduleQueryService storeScheduleQueryService;
    private final ReservationQueryService reservationQueryService;
    private final ReservationCommandService reservationCommandService;
    private final ApplicationEventPublisher applicationEventPublisher;

    /**
     * 락 내부에서 호출되는 트랜잭션 경계.
     * 락 획득 → 트랜잭션 시작 → 커밋 → 락 해제 순서를 강제합니다.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void reserveInTransaction(CreateReservationCommand command) {
        // 결식 아동 조회
        Child child = childQueryService.getChildById(new GetChildByIdQuery(command.childId()));
        // 가게 일정 조회
        StoreSchedule storeSchedule = storeScheduleQueryService.getStoreScheduleById(new GetStoreScheduleByIdQuery(command.storeScheduleId()));
        Owner owner = storeSchedule.getStore().getOwner();

        // 인원 수 검증
        if (command.people() == null || command.people() <= 0) {
            throw new CoreException(RESERVATION_PEOPLE_MUST_NOT_BE_NULL);
        }

        // 예약 인원이 최대 인원을 초과하는지 검증
        if (storeSchedule.getMaxPeople() != null && command.people() > storeSchedule.getMaxPeople()) {
            throw new CoreException(RESERVATION_PEOPLE_EXCEEDS_MAX);
        }

        // 이미 지난 시간의 일정이면 예약 불가
        if (!storeSchedule.isBookableAt(LocalDateTime.now())) {
            throw new CoreException(ErrorType.Reservation.RESERVATION_SCHEDULE_TIME_EXPIRED);
        }

        // 조회한 가게 일정이 이미 예약된 일정인지 체크 (테이블 조회해서 확인)
        Reservation reservation = reservationQueryService.getByStoreScheduleId(
                new GetByStoreScheduleIdQuery(command.storeScheduleId()));
        boolean isUnavailable = reservation != null && !reservation.isAvailable();
        if (isUnavailable) {
            throw new CoreException(ErrorType.Reservation.RESERVATION_ALREADY_EXISTS);
        }

        // 예약 생성
        Reservation saved = reservationCommandService.createReservation(command, storeSchedule, child);

        // 예약 생성 이벤트 발행
        applicationEventPublisher.publishEvent(new ReservationEvent(
                saved.getId(),
                owner.getId(),
                child.getId(),
                RESERVATION_CREATED
        ));
    }
}