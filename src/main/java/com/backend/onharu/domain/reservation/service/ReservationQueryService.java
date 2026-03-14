package com.backend.onharu.domain.reservation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindAllByStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdAndStatusFilterQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdAndStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByOwnerIdAndStatusFilterQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByStoreIdAndStatusFilterQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByStoreIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindCompletedWithoutReviewByChildIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindUpcomingByChildIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindUpcomingByOwnerIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetByStoreScheduleIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindAllByStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusFilterParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByOwnerIdAndStatusInParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdAndStatusFilterParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindUpcomingByChildIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindUpcomingByOwnerIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetByStoreScheduleIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetReservationByIdParam;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationQueryService {
    private final ReservationRepository reservationRepository;

    /**
     * 예약 단건 조회
     * 
     * @param query 예약 ID
     * @return 조회된 Reservation 엔티티 (없으면 예외 발생)
     */
    public Reservation getReservation(GetReservationByIdQuery query) {
        return reservationRepository.getReservation(
                new GetReservationByIdParam(query.reservationId()));
    }

    /**
     * 아동 ID로 예약 목록 조회
     * 
     * @param query 아동 ID
     * @return 아동 ID에 해당하는 예약 리스트
     */
    public Page<Reservation> findByChildIdAndStatusFilter(FindByChildIdAndStatusFilterQuery query, Pageable pageable) {
        return reservationRepository.findByChildIdAndStatusFilter(
                new FindByChildIdAndStatusFilterParam(query.childId(), query.statusFilters()), pageable);
    }

    /**
     * 아동 ID로 예약 목록 조회 (List 반환 - COUNT 쿼리 없음)
     * 
     * @param query 아동 ID
     * @return 아동 ID에 해당하는 예약 리스트
     */
    public List<Reservation> findByChildIdAndStatusFilterAsList(FindByChildIdAndStatusFilterQuery query, Pageable pageable) {
        return reservationRepository.findByChildIdAndStatusFilterAsList(
                new FindByChildIdAndStatusFilterParam(query.childId(), query.statusFilters()), pageable);
    }

    /**
     * 아동 ID 기준으로 리뷰 미작성 COMPLETED 예약 목록 조회
     */
    public List<Reservation> findCompletedWithoutReviewByChildId(FindCompletedWithoutReviewByChildIdQuery query, Pageable pageable) {
        return reservationRepository.findCompletedWithoutReviewByChildId(query.childId(), pageable);
    }


    /**
     * 가게 ID와 상태 필터로 예약 목록 조회 (페이징)
     * 
     * @param query 가게 ID와 상태 필터
     * @param pageable 페이징 정보
     * @return 가게 ID와 상태 필터에 해당하는 예약 리스트
     */
    public Page<Reservation> findByStoreIdAndStatusFilter(FindByStoreIdAndStatusFilterQuery query, Pageable pageable) {
        return reservationRepository.findByStoreIdAndStatusFilter(new FindByStoreIdAndStatusFilterParam(query.storeId(), query.statusFilter()), pageable);
    }

    /**
     * 사업자 ID와 상태 필터로 예약 목록 조회
     * 
     * @param query 사업자 ID와 상태 필터
     * @param pageable 페이징 정보
     * @return 사업자 ID와 상태 필터에 해당하는 예약 리스트
     */
    public List<Reservation> findByOwnerIdAndStatusIn(FindByOwnerIdAndStatusFilterQuery query, Pageable pageable) {
        return reservationRepository.findByOwnerIdAndStatusIn(new FindByOwnerIdAndStatusInParam(query.ownerId(), query.statusFilters()), pageable);
    }

    /**
     * 가게 일정 ID로 예약 단건 조회
     * 
     * @param query 가게 일정 ID
     * @return 가게 일정 ID에 해당하는 예약 엔티티
     */
    public Reservation getByStoreScheduleId(GetByStoreScheduleIdQuery query) {
        return reservationRepository.getLatestByStoreScheduleId(new GetByStoreScheduleIdParam(query.storeScheduleId()));
    }

    /**
     * 가게 ID로 예약 목록 조회
     *
     * @param query 가게 ID
     * @return 가게 ID에 해당하는 예약 리스트
     */
    public List<Reservation> findByStoreId(FindByStoreIdQuery query) {
        return reservationRepository.findByStoreId(
                new FindByStoreIdParam(query.storeId()));
    }

    /**
     * 예약 상태로 예약 목록 조회
     * 
     * @param query 예약 상태
     * @return 상태에 해당하는 예약 리스트
     */
    public List<Reservation> findAllByStatus(FindAllByStatusQuery query) {
        return reservationRepository.findAllByStatus(
                new FindAllByStatusParam(query.status()));
    }

    /**
     * 아동 ID와 상태로 예약 목록 조회
     * 
     * @param query 아동 ID와 상태
     * @return 아동 ID와 상태에 해당하는 예약 리스트
     */
    public List<Reservation> findByChildIdAndStatus(FindByChildIdAndStatusQuery query) {
        return reservationRepository.findByChildIdAndStatus(
                new FindByChildIdAndStatusParam(query.childId(), query.status()));
    }

    /**
     * 아동 ID 기준 다가오는 예약 조회 (scheduleDate >= fromDate)
     *
     * @param query    아동 ID, 상태 필터, 기준 날짜
     * @param pageable 페이징 정보 (정렬, limit 포함)
     * @return 다가오는 예약 리스트
     */
    public List<Reservation> findUpcomingByChildId(FindUpcomingByChildIdQuery query, Pageable pageable) {
        return reservationRepository.findUpcomingByChildId(
                new FindUpcomingByChildIdParam(query.childId(), query.statusFilters(), query.fromDate()), pageable);
    }

    /**
     * 사업자 ID 기준 다가오는 예약 조회 (scheduleDate >= fromDate)
     *
     * @param query    사업자 ID, 상태 필터, 기준 날짜
     * @param pageable 페이징 정보 (정렬, limit 포함)
     * @return 다가오는 예약 리스트
     */
    public List<Reservation> findUpcomingByOwnerId(FindUpcomingByOwnerIdQuery query, Pageable pageable) {
        return reservationRepository.findUpcomingByOwnerId(
                new FindUpcomingByOwnerIdParam(query.ownerId(), query.statusFilters(), query.fromDate()), pageable);
    }
}
