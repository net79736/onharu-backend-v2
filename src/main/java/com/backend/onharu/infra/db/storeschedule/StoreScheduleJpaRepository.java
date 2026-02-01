package com.backend.onharu.infra.db.storeschedule;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

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
     * 일정 날짜로 가게 일정 목록 조회
     */
    List<StoreSchedule> findAllByScheduleDate(LocalDate scheduleDate);

    /**
     * 가게 ID와 일정 날짜로 가게 일정 조회
     */
    List<StoreSchedule> findByStoreIdAndScheduleDate(Long storeId, LocalDate scheduleDate);
}
