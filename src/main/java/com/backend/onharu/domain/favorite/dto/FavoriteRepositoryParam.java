package com.backend.onharu.domain.favorite.dto;

/**
 * 찜하기 Repository 에서 사용될 파라미터 입니다.
 */
public class FavoriteRepositoryParam {

    /**
     * 찜하기 ID 로 단건 조회용 파라미터
     */
    public record GetFavoriteByIdParam(
            Long favoriteId
    ) {
    }

    /**
     * 아동 ID 로 찜하기 목록 조회용 파라미터
     */
    public record FindFavoritesByChildIdParam(
            Long childId
    ) {
    }

    /**
     * 아동 ID 와 가게 ID 로 찜하기 조회
     * @param childId 아동 ID
     * @param storeId 가게 ID
     */
    public record FindFavoriteByChildIdAndStoreIdParam(
            Long childId,
            Long storeId
    ) {
    }
}
