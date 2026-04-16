package com.backend.onharu.infra.redis.count;

/**
 * 카운트 서비스 타입.
 *
 * <p>reddit-clone의 ServiceType을 단순화해서 VIEW/CHECK만 먼저 지원합니다.</p>
 */
public enum ServiceType {
    VIEW, // +1 증가 후 현재 조회수 반환
    CHECK // 현재 조회수 반환
}

