package com.backend.onharu.infra.db.reservation;

import java.time.LocalDate;
import java.util.Collection;
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
     * 해당 가게 일정에 {@code statuses}에 포함된 상태의 예약이 하나라도 있으면 true
     */
    boolean existsByStoreSchedule_IdAndStatusIn(Long storeScheduleId, Collection<ReservationType> statuses);

    /**
     * 해당 가게 일정에 연결된 예약 행을 모두 삭제 (스케줄 삭제 전 FK 해소. 활성 예약 없음이 검증된 뒤에만 호출)
     */
    void deleteByStoreSchedule_Id(Long storeScheduleId);

    /**
     * 예약 상태로 예약 목록 조회
     */
    List<Reservation> findAllByStatus(ReservationType status);

    /**
     * 아동 ID와 상태로 예약 목록 조회
     */
    List<Reservation> findByChildIdAndStatus(Long childId, ReservationType status);

    /**
     * 아동 ID와 상태로 예약 목록 조회 (페이징)
     */
    Page<Reservation> findByChildIdAndStatus(Long childId, ReservationType status, Pageable pageable);

    /**
     * 가게 ID로 예약 목록 조회
     */
    List<Reservation> findByStoreSchedule_StoreId(Long storeId);

    /**
     * 가게 ID로 예약 목록 조회 (페이징)
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
    Page<Reservation> findLatestReservationsByStoreId(@Param("storeId") Long storeId, Pageable pageable);

    /**
     * 가게 ID와 상태로 예약 목록 조회 (페이징)
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.storeSchedule.store.id = :storeId
          AND r.status = :status
          AND r.id = (
                SELECT MAX(r2.id)
                FROM Reservation r2
                WHERE r2.storeSchedule = r.storeSchedule
          )
        """)
    Page<Reservation> findLatestReservationsByStoreIdAndStatus(@Param("storeId") Long storeId, @Param("status") ReservationType status, Pageable pageable);


    /**
     * 기한이 경과한(만료 대상) 예약을 상태별로 조회합니다.
     */
    @Query("""
    SELECT r 
      FROM Reservation r
      JOIN FETCH r.storeSchedule s
     WHERE (r.status = 'ALL' OR r.status = :status)
       AND s.scheduleDate < :date
    """)
    List<Reservation> findByStatusAndScheduleDateBeforeThan(@Param("status") ReservationType status, @Param("date") LocalDate date);

    /**
     * 아동 ID와 상태 여러 개로 예약 목록 조회 (페이징 - COUNT 쿼리 포함)
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.status IN :statuses
          AND r.child.id = :childId
        """)
    Page<Reservation> findByChildIdAndStatusIn(
        @Param("childId") Long childId,
        @Param("statuses") List<ReservationType> statuses,
        Pageable pageable
    );

    /**
     * 아동 ID와 상태 여러 개로 예약 목록 조회 (List 반환 - COUNT 쿼리 없음)
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.status IN :statuses
          AND r.child.id = :childId
        """)
    List<Reservation> findByChildIdAndStatusInAsList(
        @Param("childId") Long childId,
        @Param("statuses") List<ReservationType> statuses,
        Pageable pageable
    );


    /**
     * 사업자 ID와 상태 필터로 예약 목록 조회
     */
    @Query("""
        SELECT r
          FROM Reservation r
         WHERE r.status IN :statuses
           AND r.storeSchedule.store.owner.id = :ownerId
        """)
    List<Reservation> findByOwnerIdAndStatusIn(
            @Param("ownerId") Long ownerId,
            @Param("statuses") List<ReservationType> statuses,
            Pageable pageable);

    /**
     * 아동 ID 기준으로 리뷰가 없는 COMPLETED 예약 목록 조회
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.child.id = :childId
          AND r.status = 'COMPLETED'
          AND NOT EXISTS (
              SELECT rv FROM Review rv WHERE rv.reservation.id = r.id
          )
        """)
    List<Reservation> findCompletedWithoutReviewByChildId(
        @Param("childId") Long childId,
        Pageable pageable
    );

    /**
     * 아동 ID 기준 다가오는 예약 조회 (scheduleDate >= fromDate)
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.status IN :statuses
          AND r.child.id = :childId
          AND r.storeSchedule.scheduleDate >= :fromDate
        """)
    List<Reservation> findUpcomingByChildId(
        @Param("childId") Long childId,
        @Param("statuses") List<ReservationType> statuses,
        @Param("fromDate") LocalDate fromDate,
        Pageable pageable
    );

    /**
     * 사업자 ID 기준 다가오는 예약 조회 (scheduleDate >= fromDate)
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.status IN :statuses
          AND r.storeSchedule.store.owner.id = :ownerId
          AND r.storeSchedule.scheduleDate >= :fromDate
        """)
    List<Reservation> findUpcomingByOwnerId(
        @Param("ownerId") Long ownerId,
        @Param("statuses") List<ReservationType> statuses,
        @Param("fromDate") LocalDate fromDate,
        Pageable pageable
    );
}