package com.backend.onharu.infra.redis.cache;

import java.time.Duration;
import java.util.Collections;
import java.util.List;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.level.dto.LevelCacheDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

/**
 * 등급(Level) 목록을 Redis Hash로 캐싱합니다.
 */
@Repository
@RequiredArgsConstructor
public class LevelHashCacheRepository {

    private static final String HASH_KEY = "onharu:cache:level:list";
    private static final String FIELD_ALL = "all";
    private static final Duration TTL = Duration.ofHours(1);

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public List<LevelCacheDto> getAll() {
        String json = (String) redisTemplate.opsForHash().get(HASH_KEY, FIELD_ALL);
        if (json == null || json.isBlank()) {
            return Collections.emptyList();
        }
        try {
            return objectMapper.readValue(json, new TypeReference<>() {});
        } catch (Exception e) {
            evictAll();
            return Collections.emptyList();
        }
    }

    public void putAll(List<LevelCacheDto> levels) {
        try {
            String json = objectMapper.writeValueAsString(levels);
            redisTemplate.opsForHash().put(HASH_KEY, FIELD_ALL, json);
            redisTemplate.expire(HASH_KEY, TTL);
        } catch (Exception e) {
            // ignore
        }
    }

    public void evictAll() {
        redisTemplate.delete(HASH_KEY);
    }
}

