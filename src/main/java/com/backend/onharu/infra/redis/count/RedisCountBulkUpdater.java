package com.backend.onharu.infra.redis.count;

import java.util.Map;
import java.util.Set;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis 카운트 벌크 업데이터
 *
 * <p>reddit-clone의 {@code RedisCountBulkUpdater} 패턴을 그대로 가져온 구현입니다.</p>
 *
 * <p>동작 방식:</p>
 * <ol>
 *   <li>Redis에서 해당 도메인의 모든 키 조회</li>
 *   <li>각 키의 해시 데이터 읽기</li>
 *   <li>DB의 카운트 정보 조회</li>
 *   <li>Redis 값으로 DB 업데이트(여기서는 view delta 누적)</li>
 *   <li>Redis 키 삭제</li>
 * </ol>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RedisCountBulkUpdater {

    private final RedisTemplate<String, String> redisTemplate;
    private final CountStrategyFactory strategyFactory;

    public void bulkUpdate(DomainType type) {
        CountStrategy strategy = strategyFactory.getStrategy(type);
        if (strategy == null) {
            log.warn("⚠️ [RedisCountBulkUpdater] 지원하지 않는 도메인 타입: {}", type);
            return;
        }

        log.info("🔄 [RedisCountBulkUpdater] 벌크 업데이트 시작 - type: {}", type);
        String pattern = strategy.getRedisPattern();

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            log.info("📭 [RedisCountBulkUpdater] 대상 없음 - type: {}", type);
            return;
        }

        log.info("📊 [RedisCountBulkUpdater] 업데이트 대상: {}개 - type: {}", keys.size(), type);

        for (String key : keys) {
            try {
                Long id = strategy.extractIdFromKey(key);
                if (id == null) {
                    redisTemplate.delete(key);
                    continue;
                }

                Map<Object, Object> redisHash = redisTemplate.opsForHash().entries(key);
                if (redisHash == null || redisHash.isEmpty()) {
                    redisTemplate.delete(key);
                    continue;
                }

                // 현재는 store view만: Redis hash field "view"를 절대값으로 해석
                long viewCount = safeParseLong(redisHash.get(StoreViewCountStrategy.FIELD_VIEW), 0L);

                // DB 업데이트(전략에서 절대값으로 반영)
                strategy.updateToDatabase(id, new CommonCount(viewCount));

                redisTemplate.delete(key);

                log.info("✅ [RedisCountBulkUpdater] DB 저장 완료 - type: {}, id: {}, view: {}", type, id, viewCount);
            } catch (Exception e) {
                log.error("❌ [RedisCountBulkUpdater] 저장 실패 - key: {}, 이유: {}", key, e.getMessage(), e);
            }
        }

        log.info("✅ [RedisCountBulkUpdater] 벌크 업데이트 완료 - type: {}", type);
    }

    private long safeParseLong(Object redisValue, long fallback) {
        if (redisValue == null) return fallback;
        try {
            return Long.parseLong(redisValue.toString());
        } catch (NumberFormatException e) {
            log.warn("⚠️ [RedisCountBulkUpdater] 숫자 파싱 실패 - value: {} (fallback: {})", redisValue, fallback);
            return fallback;
        }
    }
}