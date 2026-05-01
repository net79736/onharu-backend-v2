package com.backend.onharu.infra.db.storeschedule;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.storeschedule.model.StoreSchedule;

/**
 * 가게 일정 JPA Repository
 */
public interface StoreScheduleJpaRepository extends JpaRepository<StoreSchedule, Long> {
    
    /**
     * 가게 ID로 가게 일정 목록 조회
     */
    List<StoreSchedule> findByStoreId(Long storeId);

    /**
     * 가게 ID와 일정 날짜로 가게 일정 조회
     */
    List<StoreSchedule> findAllByStoreIdAndScheduleDateOrderByStartTimeAsc(Long storeId, LocalDate scheduleDate);

    /**
     * 가게 ID와 연/월로 가게 일정 목록 조회
     */
    List<StoreSchedule> findAllByStoreIdAndScheduleDateBetweenOrderByScheduleDateAscStartTimeAsc(
            @Param("storeId") Long storeId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    /**
     * 여러 가게 ID 중, 기준 날짜(포함) 이후 스케줄이 존재하는 가게 ID 목록을 반환합니다.
     * 목록 API에서 N+1 방지를 위해 사용합니다.
     */
    @Query("""
        SELECT DISTINCT s.store.id
          FROM StoreSchedule s
         WHERE s.store.id IN :storeIds
           AND s.scheduleDate >= :today
        """)
    List<Long> findDistinctStoreIdsHavingScheduleOnOrAfter(
            @Param("storeIds") Set<Long> storeIds,
            @Param("today") LocalDate today
    );
}
