package com.backend.onharu.infra.redis.count;

import java.time.Duration;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * 가게 조회수 누적(Delta) 저장소.
 *
 * <p>요청이 발생할 때마다 DB를 바로 업데이트하지 않고, Redis Hash에 누적한 뒤 스케줄러가 벌크로 DB에 반영합니다.</p>
 *
 * <ul>
 *   <li>Key: {@code onharu:count:store:{storeId}}</li>
 *   <li>Field: {@code view}</li>
 * </ul>
 */
@Repository
@RequiredArgsConstructor
public class StoreViewCountRedisRepository {

    static final String KEY_PREFIX = StoreViewCountStrategy.KEY_PREFIX; // "onharu:count:store:"
    static final String FIELD_VIEW = StoreViewCountStrategy.FIELD_VIEW; // "view"

    // 스케줄러(5분)보다 충분히 길게: 장애/지연 시 누적치 보호
    private static final Duration TTL = Duration.ofHours(2);

    private final RedisTemplate<String, String> redisTemplate;

    public void incrementViewDelta(long storeId) {
        String key = KEY_PREFIX + storeId;
        redisTemplate.opsForHash().increment(key, FIELD_VIEW, 1);
        redisTemplate.expire(key, TTL);
    }

    public Set<String> findKeysForBulkUpdate() {
        return redisTemplate.keys(KEY_PREFIX + "*");
    }

    public Long getDelta(String key) {
        Object v = redisTemplate.opsForHash().get(key, FIELD_VIEW);
        if (v == null) return 0L;
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return 0L;
        }
    }

    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }
}