package com.backend.onharu.infra.db.store;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.store.model.StoreCount;

/**
 * StoreCount JPA Repository.
 *
 * <p>카운트(집계)성 데이터는 Store가 아닌 StoreCount에서 관리합니다.</p>
 */
public interface StoreCountJpaRepository extends JpaRepository<StoreCount, Long> {

    Optional<StoreCount> findByStoreId(Long storeId);

    /**
     * viewCount는 Redis의 "절대 조회수"가 주기적으로 flush 됩니다.
     * DB 값을 감소시키지 않기 위해 "더 큰 값일 때만" 갱신합니다.
     * 
     * clearAutomatically: 자동으로 영속성 컨텍스트 비우기
     * flushAutomatically: 영속성 컨텍스트의 변경 사항을 DB에 반영
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("UPDATE StoreCount sc SET sc.viewCount = :viewCount WHERE sc.storeId = :storeId AND sc.viewCount < :viewCount")
    int setViewCountIfGreater(@Param("storeId") Long storeId, @Param("viewCount") long viewCount);
}

