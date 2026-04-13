package com.backend.onharu.infra.redis;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * 최근 검색어 저장 (Redis List + Set).
 * 쿠폰 발급 샘플({@code RedisRepository})과 같이 Set으로 멤버십, List로 순서를 관리합니다.
 */
@Repository
@RequiredArgsConstructor
public class StoreRecentSearchRedisRepository {

    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 키워드를 최근 기록 순서(MRU)로 기록합니다. 이미 있으면 목록에서 제거 후 맨 앞에 넣습니다.
     * 용량 초과 시 List 오른쪽(RPOP) 제거에 맞춰 Set에서도 제거합니다.
     * 
     * @param listKey onharu:store:recent:list:u:[:user_id]
     * @param setKey onharu:store:recent:set:u:[:user_id]
     * @param keyword 검색 키워드
     * @param maxItems 최근 검색어 최대 개수
     * @param ttl 최근 검색어 유지 시간
     */
    public void record(String listKey, String setKey, String keyword, int maxItems, Duration ttl) {
        if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(setKey, keyword))) {
            redisTemplate.opsForList().remove(listKey, 0, keyword);
        }
        // 최근 기록 순서(MRU)로 기록
        redisTemplate.opsForList().leftPush(listKey, keyword);
        // 중복 여부 빠른 판별 및 제거 시 동기화
        redisTemplate.opsForSet().add(setKey, keyword);

        // maxItems 초과 시 List 제거에 맞춰 Set에서도 제거
        Long len = redisTemplate.opsForList().size(listKey);        
        while (len != null && len > maxItems) {
            String removed = redisTemplate.opsForList().rightPop(listKey); // List 오른쪽 제거
            if (removed != null) {
                // Set에서도 제거
                redisTemplate.opsForSet().remove(setKey, removed);
            }
            len = redisTemplate.opsForList().size(listKey); // List 크기 다시 조회
        }
        // 최근 검색어 유지 시간 설정
        if (ttl != null && !ttl.isZero() && !ttl.isNegative()) {
            redisTemplate.expire(listKey, ttl);
            redisTemplate.expire(setKey, ttl);
        }
    }

    /**
     * 최근 검색어 목록을 조회합니다.
     * 
     * @param listKey onharu:store:recent:list:u:[:user_id]
     * @param maxItems 최근 검색어 최대 개수
     * @return 최근 검색어 목록
     */
    public List<String> findRecent(String listKey, int maxItems) {
        List<String> range = redisTemplate.opsForList().range(listKey, 0, (long) maxItems - 1);
        return range != null ? range : Collections.emptyList();
    }
}
