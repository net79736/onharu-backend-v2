package com.backend.onharu.infra.redis.count;

/**
 * 공통 카운트 모델 (현재는 view만 사용).
 *
 * <p>추후 comment/like 등 확장 가능하도록 reddit-clone 스타일로 둡니다.</p>
 */
public record CommonCount(
        long viewCount
) {
}