package com.backend.onharu.interfaces.shceduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.onharu.infra.redis.count.DomainType;
import com.backend.onharu.infra.redis.count.RedisCountBulkUpdater;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
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
            // bulkUpdater.bulkUpdate(DomainType.LEVEL);
            // bulkUpdater.bulkUpdate(DomainType.OWNER);
            // bulkUpdater.bulkUpdate(DomainType.TAG);
        } catch (Exception e) {
            log.warn("[StoreViewCountScheduler] flush failed", e);
        }
    }
}