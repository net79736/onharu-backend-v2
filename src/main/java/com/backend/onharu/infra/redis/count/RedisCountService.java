package com.backend.onharu.infra.redis.count;

import static com.backend.onharu.utils.NumberUtils.toLong;

import java.time.Duration;
import java.util.Map;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 카운트 서비스 (조회수).
 *
 * <p>reddit-clone의 RedisCountService 패턴을 참고하여, cache miss 시 DB 값을 Redis에 적재한 뒤
 * Redis Hash에서만 증가시키고 스케줄러가 주기적으로 DB로 동기화합니다.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RedisCountService {

    // 스케줄러 동기화 주기(예: 5분)보다 충분히 길게 설정하여 데이터 유실 방지
    private static final Duration TTL = Duration.ofHours(2);
    private static final String FIELD_VIEW = StoreViewCountStrategy.FIELD_VIEW;
    private static final String UNSUPPORTED_DOMAIN_LOG = "⚠️ [RedisCountService] 지원하지 않는 도메인 타입: {}";
    private static final String FIELD_FAVORITE = StoreViewCountStrategy.FIELD_FAVORITE;

    private final RedisTemplate<String, String> redisTemplate;
    private final CountStrategyFactory strategyFactory;

    /**
     * 조회수 조회 및 업데이트
     * * @param type 서비스 타입 (VIEW: 증가 후 반환, CHECK: 단순 조회)
     * @param content 도메인 타입 (STORE 등)
     * @param contentId 컨텐츠 식별 ID
     * @return 현재 혹은 업데이트된 조회수
     */
    public long getViewCount(ServiceType type, DomainType content, long contentId) {
        CountStrategy strategy = strategyFactory.getStrategy(content);
        if (strategy == null) {
            log.warn(UNSUPPORTED_DOMAIN_LOG, content);
            return 0L;
        }

        String key = strategy.getRedisKey(contentId); // 예: "onharu:count:store:123"
        long currentViewCount = ensureSeededAndGet(key, strategy, contentId, FIELD_VIEW);

        // 단순 조회 모드인 경우 즉시 반환
        if (type == ServiceType.CHECK) {
            return currentViewCount;
        }

        // 4. VIEW 모드: Redis 카운트 증가 (+1)
        return incrementCount(key, FIELD_VIEW, currentViewCount);
    }

    /**
     * 안전하게 카운트를 증가시키고 결과를 반환
     */
    private long incrementCount(String key, String field, long fallbackValue) {
        try {
            long updated = redisTemplate.opsForHash().increment(key, field, 1);
            redisTemplate.expire(key, TTL); // 활동이 있을 때마다 TTL 연장
            return updated;
        } catch (Exception e) {
            log.error("❌ [RedisCountService] Increment 실패 - key: {}, error: {}", key, e.getMessage());
            return fallbackValue + 1;
        }
    }

    /**
     * 찜(좋아요) 수 조회 (증가/감소 없음).
     */
    public long getFavoriteCount(DomainType content, long contentId) {
        CountStrategy strategy = strategyFactory.getStrategy(content);
        if (strategy == null) {
            log.warn(UNSUPPORTED_DOMAIN_LOG, content);
            return 0L;
        }
        String key = strategy.getRedisKey(contentId);
        return ensureSeededAndGet(key, strategy, contentId, FIELD_FAVORITE);
    }

    /**
     * 찜(좋아요) 수를 delta(+1/-1)만큼 변경하고 변경된 절대값을 반환합니다.
     */
    public long changeFavoriteCount(DomainType content, long contentId, long delta) {
        CountStrategy strategy = strategyFactory.getStrategy(content);
        if (strategy == null) {
            log.warn(UNSUPPORTED_DOMAIN_LOG, content);
            return 0L;
        }
        String key = strategy.getRedisKey(contentId);
        long current = ensureSeededAndGet(key, strategy, contentId, FIELD_FAVORITE);
        try {
            long updated = redisTemplate.opsForHash().increment(key, FIELD_FAVORITE, delta);
            redisTemplate.expire(key, TTL);
            return Math.max(0L, updated);
        } catch (Exception e) {
            log.error("❌ [RedisCountService] Favorite 변경 실패 - key: {}, error: {}", key, e.getMessage());
            return Math.max(0L, current + delta);
        }
    }

    /**
     * 안전하게 카운트를 증가시키고 결과를 반환
     * 
     * @param key
     * @param strategy
     * @param contentId
     * @param field
     * @return
     */
    private long ensureSeededAndGet(String key, CountStrategy strategy, long contentId, String field) {
        CommonCount current = ensureSeededAndGetAll(key, strategy, contentId);
        return field.equals(FIELD_FAVORITE) ? current.favoriteCount() : current.viewCount();
    }

    /**
     * Redis Hash를 한 번에 읽고(view/favorite), 없으면 DB에서 seed 한 뒤 반환합니다.
     *
     * <p>가져온 예시 코드의 장점(해시 전체 조회 1회 + miss 시 일괄 seed)을 흡수하되,
     * onharu는 String 직렬화를 유지하여 타입 꼬임을 원천 차단합니다.</p>
     */
    private CommonCount ensureSeededAndGetAll(String key, CountStrategy strategy, long contentId) {
        Map<Object, Object> redisMap = redisTemplate.opsForHash().entries(key);
        if (!redisMap.isEmpty()
                && redisMap.containsKey(FIELD_VIEW)
                && redisMap.containsKey(FIELD_FAVORITE)) {
            long view = Math.max(0L, toLong(redisMap.get(FIELD_VIEW)));
            long favorite = Math.max(0L, toLong(redisMap.get(FIELD_FAVORITE)));
            return new CommonCount(view, favorite);
        }

        // Cache miss (또는 일부 필드 누락): DB에서 로드 후 필요한 필드들을 한 번에 seed
        log.info("📭 [RedisCountService] Cache Miss - key: {}", key);
        CommonCount dbValue = strategy.loadFromDatabase(contentId);

        long view = Math.max(0L, dbValue.viewCount());
        long favorite = Math.max(0L, dbValue.favoriteCount());
        redisTemplate.opsForHash().put(key, FIELD_VIEW, String.valueOf(view));
        redisTemplate.opsForHash().put(key, FIELD_FAVORITE, String.valueOf(favorite));
        redisTemplate.expire(key, TTL);

        return new CommonCount(view, favorite);
    }

    /**
     * 특정 컨텐츠의 카운트 캐시 삭제
     * * @param content 도메인 타입
     * @param contentId 컨텐츠 ID
     */
    public void removeCount(DomainType content, long contentId) {
        CountStrategy strategy = strategyFactory.getStrategy(content);
        if (strategy == null) return;
        
        String key = strategy.getRedisKey(contentId);
        redisTemplate.delete(key);
        log.info("🗑️ [RedisCountService] 캐시 삭제 완료 - key: {}", key);
    }
}