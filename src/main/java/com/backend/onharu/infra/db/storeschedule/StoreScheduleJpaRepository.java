package com.backend.onharu.infra.db.storeschedule;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.storeschedule.model.StoreSchedule;

/**
 * 가게 일정 JPA Repository
 */
public interface StoreScheduleJpaRepository extends JpaRepository<StoreSchedule, Long> {
}
