package com.backend.onharu.infra.db.reservation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.reservation.model.Reservation;

/**
 * 예약 JPA Repository
 */
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
    
    /**
     * 아동 ID로 예약 목록 조회
     */
    List<Reservation> findByChildId(Long childId);

    /**
     * 가게 일정 ID로 예약 단건 조회
     */
    Optional<Reservation> getByStoreScheduleId(Long storeScheduleId);

    /**
     * 예약 상태로 예약 목록 조회
     */
    List<Reservation> findAllByStatus(ReservationType status);

    /**
     * 아동 ID와 상태로 예약 목록 조회
     */
    List<Reservation> findByChildIdAndStatus(Long childId, ReservationType status);

    /**
     * 가게 ID로 예약 목록 조회
     */
    List<Reservation> findByStoreSchedule_StoreId(Long storeId);

    /**
     * 가게 ID와 상태로 예약 목록 조회
     */
    List<Reservation> findByStoreSchedule_StoreIdAndStatus(Long storeId, ReservationType status);
}