package com.backend.onharu.infra.redis.count;

/**
 * Redis 카운트 동기화 전략.
 *
 * <p>도메인별 Redis key 규칙, DB 로딩/업데이트를 캡슐화합니다.</p>
 */
public interface CountStrategy {
    /**
     * 이 전략이 지원하는 도메인 타입.
     */
    DomainType getSupportedDomain();

    String getRedisPattern(); // Redis 키 패턴

    String getRedisKey(long id); // Redis 키

    Long extractIdFromKey(String key); // Redis 키에서 ID 추출

    CommonCount loadFromDatabase(long id); // DB에서 카운트 로드

    void updateToDatabase(long id, CommonCount count); // DB에 카운트 업데이트
}