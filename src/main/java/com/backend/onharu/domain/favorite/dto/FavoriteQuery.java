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

    /**
     * 찜하기 내역 조회
     * @param childId 아동 ID
     * @param storeId 가게 ID
     */
    public record FindFavoriteByChild_IdAndStore_IdQuery(
            Long childId,
            Long storeId
    ) {
    }
}
