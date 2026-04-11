package com.backend.onharu.infra.db.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.store.model.BusinessHours;

/**
 * 가게 영업시간 JPA Repository
 */
public interface BusinessHoursJpaRepository extends JpaRepository<BusinessHours, Long> {

    @Query("""
        SELECT bh
          FROM BusinessHours bh
         WHERE bh.store.id IN :storeIds
        """)
    List<BusinessHours> findAllByStoreIds(@Param("storeIds") List<Long> storeIds);
}