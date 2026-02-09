package com.backend.onharu.domain.favorite.dto;

/**
 * 찜하기 관련 Query DTO
 */
public class FavoriteQuery {

    /**
     * 찜하기 ID 로 단건 조회
     */
    public record GetFavoriteByIdQuery(
            Long favoriteId
    ) {
    }

    /**
     * 아동 ID 로 찜하기 목록 조회
     */
    public record FindFavoritesByChildIdQuery(
            Long childId
    ) {
    }
}
