package com.backend.onharu.domain.favorite.dto;

/**
 * 찜하기 관련 Command DTO
 */
public class FavoriteCommand {

    /**
     * 찜하기 생성(등록) Command
     */
    public record CreateFavoriteCommand(
            Long childId,
            Long storeId
    ) {
    }

    /**
     * 찜하기 취소(삭제) Command
     */
    public record DeleteFavoriteCommand(
            Long childId,
            Long favoriteId
    ) {
    }
}
