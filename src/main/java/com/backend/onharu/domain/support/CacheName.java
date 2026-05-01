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

    /**
     * 가게 카테고리 목록 캐시.
     * - key 예: {@code all}
     * - value: {@code List<CategoryCacheDto>}
     */
    public static final String STORE_CATEGORY_LIST = "storeCategoryList";

    /**
     * 등급 목록 캐시.
     * - key 예: {@code all}
     * - value: {@code List<Level>}
     */
    public static final String LEVEL_LIST = "levelList";

    private CacheName() {
    }
}
