package com.backend.onharu.domain.store.service;

import org.springframework.stereotype.Service;

import com.backend.onharu.infra.redis.count.DomainType;
import com.backend.onharu.infra.redis.count.RedisCountService;
import com.backend.onharu.infra.redis.count.ServiceType;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreViewCountService {

    private final RedisCountService redisCountService;

    /**
     * 가게 상세 화면 진입(조회) 시 조회수 +1을 Redis에 반영하고, 현재 조회수를 반환합니다.
     */
    public long recordViewAndGet(long storeId) {
        return redisCountService.getViewCount(ServiceType.VIEW, DomainType.STORE, storeId);
    }
}