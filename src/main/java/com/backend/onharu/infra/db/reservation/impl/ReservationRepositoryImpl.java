package com.backend.onharu.infra.db.reservation.impl;

import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_NOT_FOUND;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindAllByStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusFilterParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByOwnerIdAndStatusInParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStatusAndScheduleDateBeforeThanParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdAndStatusFilterParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindUpcomingByChildIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindUpcomingByOwnerIdParam;
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
        List<ReservationType> statuses = resolveStatuses(param.statusFilters());
        return reservationJpaRepository.findByChildIdAndStatusIn(param.childId(), statuses, pageable);
    }

    @Override
    public List<Reservation> findByChildIdAndStatusFilterAsList(FindByChildIdAndStatusFilterParam param, Pageable pageable) {
        List<ReservationType> statuses = resolveStatuses(param.statusFilters());
        return reservationJpaRepository.findByChildIdAndStatusInAsList(param.childId(), statuses, pageable);
    }

    @Override
    public List<Reservation> findCompletedWithoutReviewByChildId(Long childId, Pageable pageable) {
        return reservationJpaRepository.findCompletedWithoutReviewByChildId(childId, pageable);
    }

    @Override
    public Reservation getLatestByStoreScheduleId(GetByStoreScheduleIdParam param) {
        return reservationJpaRepository.getLatestByStoreScheduleId(param.storeScheduleId()).orElse(null);
    }

    @Override
    public boolean existsByStoreScheduleIdAndStatusIn(Long storeScheduleId, Collection<ReservationType> statuses) {
        return reservationJpaRepository.existsByStoreSchedule_IdAndStatusIn(storeScheduleId, statuses);
    }

    @Override
    public void deleteAllByStoreScheduleId(Long storeScheduleId) {
        reservationJpaRepository.deleteByStoreSchedule_Id(storeScheduleId);
    }

    @Override
    public List<Reservation> findByStoreId(FindByStoreIdParam param) {
        return reservationJpaRepository.findByStoreSchedule_StoreId(param.storeId());
    }

    @Override
    public Page<Reservation> findByStoreIdAndStatusFilter(FindByStoreIdAndStatusFilterParam param, Pageable pageable) {
        return param.statusFilter()
                .map(status -> reservationJpaRepository.findLatestReservationsByStoreIdAndStatus(param.storeId(), status, pageable))
                .orElseGet(() -> reservationJpaRepository.findLatestReservationsByStoreId(param.storeId(), pageable));
    }

    @Override
    public List<Reservation> findByOwnerIdAndStatusIn(FindByOwnerIdAndStatusInParam param, Pageable pageable) {
        List<ReservationType> statuses = resolveStatuses(param.statusFilters());
        return reservationJpaRepository.findByOwnerIdAndStatusIn(param.ownerId(), statuses, pageable);
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

    @Override
    public List<Reservation> findUpcomingByChildId(FindUpcomingByChildIdParam param, Pageable pageable) {
        List<ReservationType> statuses = resolveStatuses(param.statusFilters());
        return reservationJpaRepository.findUpcomingByChildId(param.childId(), statuses, param.fromDate(), pageable);
    }

    @Override
    public List<Reservation> findUpcomingByOwnerId(FindUpcomingByOwnerIdParam param, Pageable pageable) {
        List<ReservationType> statuses = resolveStatuses(param.statusFilters());
        return reservationJpaRepository.findUpcomingByOwnerId(param.ownerId(), statuses, param.fromDate(), pageable);
    }

    /**
     * statusFilters가 비어 있으면 전체 ReservationType 목록을 반환합니다.
     * JPQL의 IN 조건은 빈 컬렉션을 허용하지 않으므로, "전체 조회(ALL)" 의도를 표현할 때
     * 모든 상태값을 직접 전달하는 방식으로 처리합니다.
     */
    private List<ReservationType> resolveStatuses(List<ReservationType> statusFilters) {
        if (statusFilters == null || statusFilters.isEmpty()) {
            return Arrays.asList(ReservationType.values());
        }
        return statusFilters;
    }
}
