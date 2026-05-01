package com.backend.onharu.infra.redis.count;

import java.util.Map;

/**
 * Redis 카운트 동기화 전략.
 *
 * <p>도메인별 Redis key 규칙, DB 로딩/업데이트를 캡슐화합니다.</p>
 */
public interface CountStrategy {
    
    DomainType getSupportedDomain(); // 이 전략이 지원하는 도메인 타입

    String getRedisPattern(); // Redis 키 패턴

    String getRedisKey(long id); // Redis 키

    Long extractIdFromKey(String key); // Redis 키에서 ID 추출

     // Redis Hash 데이터를 도메인 카운트로 변환합니다.
     // 도메인별 field 이름/파싱 규칙은 전략 내부로 캡슐화합니다.
    CommonCount readFromRedisHash(Map<Object, Object> redisHash);

    CommonCount loadFromDatabase(long id); // DB에서 카운트 로드

    void updateToDatabase(long id, CommonCount count); // DB에 카운트 업데이트
}