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
     * 가게 상세 정보 조회 (radius는 nullable 옵션)
     */
    public record GetStoreQuery(
        Long storeId,
        Double lat,
        Double lng
    ) {
        // 위/경도 유무 확인
        public boolean hasLocation() {
            return lat != null || lng != null;
        }
    }

    /**
     * 가게 목록 조회 (위치 기반 검색)
     */
    public record SearchStoresQuery(
            Double lat,
            Double lng,
            Long categoryId,
            String keyword
    ) {
        // 위/경도 유무 확인
        public boolean hasLocation() {
            return lat != null || lng != null;
        }
    }

    /**
     * 사업자 ID로 가게 목록 조회
     */
    public record FindWithCategoryAndFavoriteCountByOwnerIdQuery(
            Long ownerId
    ) {
    }

    /**
     * 사업자 ID 로 가게(Store) 만 조회
     * @param ownerId 사업자 ID
     */
    public record FindByOwnerIdQuery(
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
