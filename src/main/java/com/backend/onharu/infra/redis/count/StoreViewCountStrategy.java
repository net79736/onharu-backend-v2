package com.backend.onharu.infra.redis.count;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.infra.db.store.StoreJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * Store 조회수 동기화 전략.
 *
 * <p>Redis Hash field "view" 값을 DB의 stores.view_count에 반영합니다.</p>
 */
@Component
@RequiredArgsConstructor
public class StoreViewCountStrategy implements CountStrategy {

    static final String KEY_PREFIX = "onharu:count:store:";
    static final String PATTERN = KEY_PREFIX + "*";
    static final String FIELD_VIEW = "view";

    private final StoreJpaRepository storeJpaRepository;

    @Override
    public DomainType getSupportedDomain() {
        return DomainType.STORE; // STORE 도메인 지원
    }

    @Override
    public String getRedisPattern() {
        return PATTERN; // "onharu:count:store:*"
    }

    @Override
    public String getRedisKey(long id) {
        return KEY_PREFIX + id; // "onharu:count:store:123"
    }

    @Override
    public Long extractIdFromKey(String key) {
        if (key == null || !key.startsWith(KEY_PREFIX)) return null;
        String raw = key.substring(KEY_PREFIX.length());
        try {
            return Long.parseLong(raw); // 123
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CommonCount loadFromDatabase(long id) {
        Store store = storeJpaRepository.findById(id).orElse(null);
        long v = (store != null && store.getViewCount() != null) ? store.getViewCount() : 0L;
        return new CommonCount(v);
    }

    @Override
    @Transactional
    public void updateToDatabase(long id, CommonCount count) {
        // count.viewCount는 Redis Hash의 "절대 조회수"로 취급합니다. (감소 방지)
        storeJpaRepository.setViewCountIfGreater(id, count.viewCount());
    }
}