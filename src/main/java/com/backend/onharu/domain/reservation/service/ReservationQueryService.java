package com.backend.onharu.domain.reservation.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindAllByStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindAllByStoreIdAndStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdAndStatusQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByChildIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByStoreIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetByStoreScheduleIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.GetReservationByIdQuery;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindAllByChildIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindAllByStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdAndStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdParam;
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
    public Page<Reservation> findByChildId(FindByChildIdQuery query, Pageable pageable) {
        return reservationRepository.findByChildId(
                new FindAllByChildIdParam(query.childId()), pageable);
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
     * 가게 ID로 store_schedule별 가장 최근 예약 1건씩 조회
     */
    public List<Reservation> findLatestReservationsByStoreId(Long storeId) {
        return reservationRepository.findLatestReservationsByStoreId(storeId);
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
     * 가게 ID와 상태로 예약 목록 조회
     * 
     * @param query 가게 ID와 상태
     * @return 가게 ID와 상태에 해당하는 예약 리스트
     */
    public List<Reservation> findByStoreIdAndStatus(FindAllByStoreIdAndStatusQuery query) {
        return reservationRepository.findByStoreIdAndStatus(
                new FindByStoreIdAndStatusParam(query.storeId(), query.status()));
    }
}
