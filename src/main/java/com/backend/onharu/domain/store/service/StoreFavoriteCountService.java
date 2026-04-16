package com.backend.onharu.domain.store.service;

import org.springframework.stereotype.Service;

import com.backend.onharu.infra.redis.count.DomainType;
import com.backend.onharu.infra.redis.count.RedisCountService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreFavoriteCountService {

    private final RedisCountService redisCountService;

    /**
     * 현재 찜(좋아요) 수를 조회합니다. (증가/감소 없음)
     */
    public long getFavoriteCount(long storeId) {
        return redisCountService.getFavoriteCount(DomainType.STORE, storeId);
    }

    /**
     * 찜 등록/취소에 따라 찜 수를 delta(+1/-1)만큼 변경합니다.
     */
    public long changeFavoriteCount(long storeId, long delta) {
        return redisCountService.changeFavoriteCount(DomainType.STORE, storeId, delta);
    }
}