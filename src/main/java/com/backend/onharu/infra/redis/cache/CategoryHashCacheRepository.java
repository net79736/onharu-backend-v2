package com.backend.onharu.infra.redis.cache;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.store.dto.CategoryCacheDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * 카테고리 목록을 Redis Hash로 캐싱합니다.
 *
 * <p>키 1개에 필드로 구분하는 Hash 구조를 사용합니다.</p>
 * - Redis Key: {@value #HASH_KEY}
 * - Field: {@value #FIELD_ALL}
 * - Value: JSON 문자열
 */
@Repository
@RequiredArgsConstructor
public class CategoryHashCacheRepository {

    private static final String HASH_KEY = "onharu:cache:store:categories";
    private static final String FIELD_ALL = "all";

    // 카테고리는 변경이 매우 드물어 길게 잡아도 무방 (필요 시 정책으로 분리)
    private static final Duration TTL = Duration.ofHours(6);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public List<CategoryCacheDto> getAll() {
        String json = (String) redisTemplate.opsForHash().get(HASH_KEY, FIELD_ALL);
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            // 역직렬화 실패 시 캐시를 비우고 DB 재적재 유도
            evictAll();
            return Collections.emptyList();
        }
    }

    public void putAll(List<CategoryCacheDto> categories) {
        try {
            String json = objectMapper.writeValueAsString(categories);
            redisTemplate.opsForHash().put(HASH_KEY, FIELD_ALL, json);
            redisTemplate.expire(HASH_KEY, TTL);
        } catch (Exception e) {
            // 캐시 저장 실패는 기능 실패가 아니므로 무시 (DB 경로는 정상 동작)
        }
    }

    public void evictAll() {
        redisTemplate.delete(HASH_KEY);
    }
}

