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
            log.warn("⚠️ [RedisCountService] 지원하지 않는 도메인 타입: {}", content);
            return 0L;
        }

        String key = strategy.getRedisKey(contentId); // 예: "onharu:count:store:123"

        // 1. Redis Hash 데이터 전체 조회
        Map<Object, Object> redisMap = redisTemplate.opsForHash().entries(key);
        long currentViewCount;

        if (redisMap == null || redisMap.isEmpty()) { 
            // 2. Cache Miss: DB에서 데이터를 가져와 Redis에 초기값 설정 (Seed)
            log.info("📭 [RedisCountService] Cache Miss - domain: {}, id: {}", content, contentId);
            CommonCount dbValue = strategy.loadFromDatabase(contentId);
            currentViewCount = dbValue.viewCount();

            redisTemplate.opsForHash().put(key, FIELD_VIEW, String.valueOf(currentViewCount));
            redisTemplate.expire(key, TTL);
        } else {
            // 3. Cache Hit
            currentViewCount = toLong(redisMap.get(FIELD_VIEW));
        }

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
            Long updated = redisTemplate.opsForHash().increment(key, field, 1);
            redisTemplate.expire(key, TTL); // 활동이 있을 때마다 TTL 연장
            return updated != null ? updated : (fallbackValue + 1);
        } catch (Exception e) {
            log.error("❌ [RedisCountService] Increment 실패 - key: {}, error: {}", key, e.getMessage());
            return fallbackValue + 1;
        }
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