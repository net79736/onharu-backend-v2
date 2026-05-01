package com.backend.onharu.domain.store.dto;

import com.backend.onharu.domain.store.model.Store;

/**
 * 가게 + 거리 + 찜 개수 조회 결과용 도메인 DTO.
 */
public record StoreWithFavoriteCount(
        Store store,
        Double distance,
        Long favoriteCount
) {
    /**
     * 거리 없이 찜 개수만 포함
     */
    public StoreWithFavoriteCount(Store store, Long favoriteCount) {
        this(store, null, favoriteCount != null ? favoriteCount : 0L);
    }

    /**
     * 거리와 찜 개수 모두 포함
     */
    public StoreWithFavoriteCount(Store store, Double distance, Long favoriteCount) {
        this.store = store;
        this.distance = distance;
        this.favoriteCount = favoriteCount != null ? favoriteCount : 0L;
    }
}
