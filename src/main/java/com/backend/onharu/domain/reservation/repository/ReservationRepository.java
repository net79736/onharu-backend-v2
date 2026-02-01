package com.backend.onharu.domain.reservation.repository;

import java.util.List;

import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindAllByChildIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindAllByStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByChildIdAndStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdAndStatusParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.FindByStoreIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetByStoreScheduleIdParam;
import com.backend.onharu.domain.reservation.dto.ReservationRepositroyParam.GetReservationByIdParam;
import com.backend.onharu.domain.reservation.model.Reservation;

public interface ReservationRepository {

    /**
     * 예약 저장 및 수정
     */
    Reservation save(Reservation reservation);

    /**
     * 예약 단건 조회
     */
    Reservation getReservation(GetReservationByIdParam param);

    /**
     * 아동 ID로 예약 목록 조회
     */
    List<Reservation> findByChildId(FindAllByChildIdParam param);

    /**
     * 가게 일정 ID로 예약 단건 조회
     */
    Reservation getByStoreScheduleId(GetByStoreScheduleIdParam param);

    /**
     * 가게 ID로 예약 목록 조회
     */
    List<Reservation> findByStoreId(FindByStoreIdParam param);

    /**
     * 예약 상태로 예약 목록 조회
     */
    List<Reservation> findAllByStatus(FindAllByStatusParam param);

    /**
     * 아동 ID와 상태로 예약 목록 조회
     */
    List<Reservation> findByChildIdAndStatus(FindByChildIdAndStatusParam param);

    /**
     * 가게 ID와 상태로 예약 목록 조회
     */
    List<Reservation> findByStoreIdAndStatus(FindByStoreIdAndStatusParam param);

    /**
     * 예약 삭제
     */
    void delete(Reservation reservation);
}
