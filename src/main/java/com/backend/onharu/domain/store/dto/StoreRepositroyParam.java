package com.backend.onharu.domain.store.dto;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

public class StoreRepositroyParam {
    /**
     * 가게 ID로 단건 조회용 파라미터
     */
    public record GetStoreByIdParam(
            Long storeId
    ) {
        public GetStoreByIdParam {
            if (storeId == null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 상세 정보 조회용 파라미터
     */
    public record GetStoreDetailByIdParam(
        Long storeId
    ) {
        public GetStoreDetailByIdParam {
            if (storeId == null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 상세 정보 조회용 파라미터
     */
    public record GetStoreDetailByIdAndLocationParam(
            Long storeId,
            Double lat,
            Double lng
    ) {
        public GetStoreDetailByIdAndLocationParam {
            if (storeId == null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    public record FindAllWithCategoryAndFavoriteCountParam(
            Long categoryId,
            String keyword
    ) {
    }

    /**
     * 페이징된 가게 목록 조회 (위치 기반 검색) 파라미터
     */
    public record FindWithCategoryAndFavoriteCountByLocationParam(
            Double lat,
            Double lng,
            Double radius,
            Long categoryId,
            String keyword
    ) {
    }
    /**
     * 사업자 ID로 가게 목록 조회용 파라미터
     */
    public record FindByOwnerIdParam(
            Long ownerId
    ) {
        public FindByOwnerIdParam {
            if (ownerId == null) {
                throw new CoreException(ErrorType.Owner.OWNER_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 카테고리 ID로 가게 목록 조회용 파라미터
     */
    public record FindByCategoryIdParam(
            Long categoryId
    ) {
        public FindByCategoryIdParam {
            if (categoryId == null) {
                throw new CoreException(ErrorType.Category.CATEGORY_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 이름으로 검색용 파라미터
     */
    public record FindByNameParam(
            String name
    ) {
    }
}
