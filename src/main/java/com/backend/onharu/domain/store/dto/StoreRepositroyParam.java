package com.backend.onharu.domain.store.dto;

public class StoreRepositroyParam {
    /**
     * 가게 ID로 단건 조회용 파라미터
     */
    public record GetStoreByIdParam(
            Long storeId
    ) {
    }

    /**
     * 사업자 ID로 가게 목록 조회용 파라미터
     */
    public record FindByOwnerIdParam(
            Long ownerId
    ) {
    }

    /**
     * 카테고리 ID로 가게 목록 조회용 파라미터
     */
    public record FindByCategoryIdParam(
            Long categoryId
    ) {
    }

    /**
     * 가게 이름으로 검색용 파라미터
     */
    public record FindByNameParam(
            String name
    ) {
    }
}
