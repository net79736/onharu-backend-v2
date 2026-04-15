package com.backend.onharu.domain.support;

/**
 * 캐시 이름 상수 클래스.
 *
 * <p>Spring Cache 어노테이션(@Cacheable/@CacheEvict 등)에서 사용하는 cacheNames를 한 곳에서 관리합니다.</p>
 */
public final class CacheName {

    /**
     * 가게 상세 캐시.
     * - key 예: {@code storeId:{storeId}}
     * - value: {@code StoreCacheDto}
     */
    public static final String STORE_DETAIL = "storeDetail";

    private CacheName() {
    }
}
