package com.backend.onharu.application;

import java.time.Duration;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Service;

import com.backend.onharu.infra.redis.StoreRecentSearchRedisKeyUtil;
import com.backend.onharu.infra.redis.StoreRecentSearchRedisRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@ConditionalOnBean(StoreRecentSearchRedisRepository.class)
public class StoreRecentSearchService {

    private final StoreRecentSearchRedisRepository repository;

    @Value("${store.search.recent-keywords.max-items:10}")
    private int maxItems; // 최근 검색어 최대 개수

    @Value("${store.search.recent-keywords.max-keyword-length:100}")
    private int maxKeywordLength; // 최근 검색어 최대 길이

    @Value("${store.search.recent-keywords.ttl-seconds:30}")
    private int ttlSeconds; // 최근 검색어 유지 시간

    /**
     * 최근 검색어 목록을 조회합니다.
     * 
     * @param ownerKey u:[:user_id]
     * @return
     */
    public List<String> list(String ownerKey) {
        String listKey = StoreRecentSearchRedisKeyUtil.listKey(ownerKey);
        return repository.findRecent(listKey, Math.max(1, maxItems));
    }

    /**
     * 최근 검색어를 저장합니다.
     * 
     * @param ownerKey u:[:user_id]
     * @param rawKeyword 원본 검색어
     */
    public void saveRecord(String ownerKey, String rawKeyword) {
        String kw = normalize(rawKeyword);
        if (kw.isEmpty()) {
            return;
        }
        String setKey = StoreRecentSearchRedisKeyUtil.setKey(ownerKey); // onharu:store:recent:set:u:[:user_id]
        String listKey = StoreRecentSearchRedisKeyUtil.listKey(ownerKey); // onharu:store:recent:list:u:[:user_id]
        Duration ttl = Duration.ofSeconds(Math.max(1, ttlSeconds));
        repository.saveRecord(listKey, setKey, kw, Math.max(1, maxItems), ttl);
    }

    /**
     * 원본 검색어를 정규화합니다.
     * 
     * @param raw 원본 검색어
     * @return 정규화된 검색어
     */
    private String normalize(String raw) {
        if (raw == null) {
            return "";
        }
        String t = raw.trim();
        if (t.length() > maxKeywordLength) {
            t = t.substring(0, maxKeywordLength);
        }
        return t;
    }
}
