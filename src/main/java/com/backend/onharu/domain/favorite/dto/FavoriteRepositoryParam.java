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
}
