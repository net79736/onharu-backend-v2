package com.backend.onharu.infra.redis.count;

/**
 * 공통 카운트 모델.
 *
 * <p>Store 기준으로 조회수(view), 찜(좋아요) 수(favorite)를 함께 관리합니다.</p>
 */
public record CommonCount(
        long viewCount,
        long favoriteCount
) {
}