package com.backend.onharu.infra.db.reservation.impl;

import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_NOT_FOUND;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindAllByStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusFilterParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStatusAndScheduleDateBeforeThanParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdAndStatusFilterParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetByStoreScheduleIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetReservationByIdParam;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.repository.ReservationRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 예약 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class ReservationRepositoryImpl implements ReservationRepository {

    private final ReservationJpaRepository reservationJpaRepository;

    @Override
    public Reservation save(Reservation reservation) {
        return reservationJpaRepository.save(reservation);
    }

    @Override
    public Reservation getReservation(GetReservationByIdParam param) {
        return reservationJpaRepository.findById(param.reservationId())
                .orElseThrow(() -> new CoreException(RESERVATION_NOT_FOUND));
    }

    @Override
    public Page<Reservation> findByChildIdAndStatusFilter(FindByChildIdAndStatusFilterParam param, Pageable pageable) {
        // statusFilter가 null이면 ALL로 간주
        return param.statusFilter().toReservationType()
                .map(status -> reservationJpaRepository.findByChildIdAndStatus(param.childId(), status, pageable))
                .orElseGet(() -> reservationJpaRepository.findByChildId(param.childId(), pageable));
    }

    @Override
    public Reservation getLatestByStoreScheduleId(GetByStoreScheduleIdParam param) {
        return reservationJpaRepository.getLatestByStoreScheduleId(param.storeScheduleId()).orElse(null);
    }

    @Override
    public List<Reservation> findByStoreId(FindByStoreIdParam param) {
        return reservationJpaRepository.findByStoreSchedule_StoreId(param.storeId());
    }

    @Override
    public Page<Reservation> findByStoreIdAndStatusFilter(FindByStoreIdAndStatusFilterParam param, Pageable pageable) {
        // statusFilter가 null이면 ALL로 간주
        return param.statusFilter().toReservationType()
                .map(status -> reservationJpaRepository.findLatestReservationsByStoreIdAndStatus(param.storeId(), status, pageable))
                .orElseGet(() -> reservationJpaRepository.findLatestReservationsByStoreId(param.storeId(), pageable));
    }

    @Override
    public List<Reservation> findAllByStatus(FindAllByStatusParam param) {
        return reservationJpaRepository.findAllByStatus(param.status());
    }

    @Override
    public List<Reservation> findByChildIdAndStatus(FindByChildIdAndStatusParam param) {
        return reservationJpaRepository.findByChildIdAndStatus(param.childId(), param.status());
    }

    @Override
    public void delete(Reservation reservation) {
        reservationJpaRepository.delete(reservation);
    }

    @Override
    public List<Reservation> findByStatusAndScheduleDateBeforeThan(FindByStatusAndScheduleDateBeforeThanParam param) {
        return reservationJpaRepository.findByStatusAndScheduleDateBeforeThan(param.status(), param.date());
    }
}
