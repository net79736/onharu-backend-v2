package com.backend.onharu.interfaces.scheduler;

import org.springframework.scheduling.annotation.Scheduled;

import com.backend.onharu.infra.redis.count.DomainType;
import com.backend.onharu.infra.redis.count.RedisCountBulkUpdater;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
// @Component
@RequiredArgsConstructor
public class StoreViewCountScheduler {

    private final RedisCountBulkUpdater bulkUpdater;

    /**
     * 5분마다 Redis 누적 조회수를 DB에 반영합니다.
     */    
    @Scheduled(fixedRate = 5 * 60 * 1000, initialDelay = 60 * 1000)
    public void flushStoreViewCounts() {
        try {
            bulkUpdater.bulkUpdate(DomainType.STORE);
        } catch (Exception e) {
            log.warn("[StoreViewCountScheduler] flush failed", e);
        }
    }
}