package com.backend.onharu.infra.db.reservation;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.reservation.model.Reservation;

/**
 * 예약 JPA Repository
 */
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long> {
    
    /**
     * 아동 ID로 예약 목록 조회
     */
    Page<Reservation> findByChildId(Long childId, Pageable pageable);

    /**
     * 가게 일정 ID로 가장 최근 예약 1건 조회
     * 동일 슬롯에 여러 건 있을 수 있으므로 id 최신순 1건만 반환
     */
    @Query("""
            SELECT r FROM Reservation r
            WHERE r.storeSchedule.id = :storeScheduleId
              AND r.id = (SELECT MAX(r2.id) FROM Reservation r2 WHERE r2.storeSchedule.id = :storeScheduleId)
            """)
    Optional<Reservation> getLatestByStoreScheduleId(@Param("storeScheduleId") Long storeScheduleId);

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

    /**
     * 가게 ID로 store_schedule별 가장 최근 예약 1건씩 조회
     * 취소 후 재예약 시 최신 건만 반환
     */
    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.storeSchedule.store.id = :storeId
              AND r.id = (
                    SELECT MAX(r2.id)
                    FROM Reservation r2
                    WHERE r2.storeSchedule = r.storeSchedule
              )
            """)
    List<Reservation> findLatestReservationsByStoreId(@Param("storeId") Long storeId);
}