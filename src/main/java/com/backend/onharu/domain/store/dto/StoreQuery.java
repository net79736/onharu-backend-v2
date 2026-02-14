package com.backend.onharu.domain.store.dto;

public class StoreQuery {
    /**
     * 가게 ID로 단건 조회
     */
    public record GetStoreByIdQuery(
            Long storeId
    ) {
    }

    /**
     * 가게 목록 조회 (위치 기반 검색)
     */
    public record SearchStoresQuery(
            Double lat,
            Double lng,
            Long categoryId
    ) {
    }

    /**
     * 사업자 ID로 가게 목록 조회
     */
    public record FindWithCategoryAndFavoriteCountByOwnerIdQuery(
            Long ownerId
    ) {
    }

    /**
     * 카테고리 ID로 가게 목록 조회
     */
    public record FindByCategoryIdQuery(
            Long categoryId
    ) {
    }

    /**
     * 가게 이름으로 검색
     */
    public record FindByNameQuery(
            String name
    ) {
    }
}
