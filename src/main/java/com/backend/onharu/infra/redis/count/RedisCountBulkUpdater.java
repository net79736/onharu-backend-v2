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
        // 1. 전략 조회 
        // 현재 StoreViewCountStrategy 하나만 존재함
        CountStrategy strategy = strategyFactory.getStrategy(type);
        if (strategy == null) {
            log.warn("⚠️ [RedisCountBulkUpdater] 지원하지 않는 도메인 타입: {}", type);
            return;
        }

        // 2. Redis 키 패턴 조회
        log.info("🔄 [RedisCountBulkUpdater] 벌크 업데이트 시작 - type: {}", type);
        String pattern = strategy.getRedisPattern();

        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            log.info("📭 [RedisCountBulkUpdater] 대상 없음 - type: {}", type);
            return;
        }

        log.info("📊 [RedisCountBulkUpdater] 업데이트 대상: {}개 - type: {}", keys.size(), type);

        for (String key : keys) {
            processKey(type, strategy, key);
        }

        log.info("✅ [RedisCountBulkUpdater] 벌크 업데이트 완료 - type: {}", type);
    }

    private void processKey(DomainType type, CountStrategy strategy, String key) {
        try {
            // 1. 사전 검증 및 잘못된 데이터 삭제 (분리된 함수 호출)
            if (isInvalidAndDeleted(key, strategy)) {
                return;
            }
    
            // 2. 본 작업 (이제 데이터가 안전하다는 것을 알고 진행)
            Long id = strategy.extractIdFromKey(key);
            Map<Object, Object> redisHash = redisTemplate.opsForHash().entries(key);
            
            CommonCount count = strategy.readFromRedisHash(redisHash);
            strategy.updateToDatabase(id, count);
            
            redisTemplate.delete(key); // 작업 성공 후 삭제
    
            log.info("✅ [RedisCountBulkUpdater] DB 저장 완료 - type: {}, id: {}", type, id);
        } catch (Exception e) {
            log.error("❌ [RedisCountBulkUpdater] 저장 실패 - key: {}, 이유: {}", key, e.getMessage());
        }
    }
    
    /**
     * 키가 유효하지 않거나 데이터가 없으면 Redis에서 삭제하고 true를 반환합니다.
     */
    private boolean isInvalidAndDeleted(String key, CountStrategy strategy) {
        Long id = strategy.extractIdFromKey(key);
        Map<Object, Object> redisHash = redisTemplate.opsForHash().entries(key);
    
        if (id == null || redisHash == null || redisHash.isEmpty()) {
            redisTemplate.delete(key);
            return true;
        }
        
        return false;
    }
}