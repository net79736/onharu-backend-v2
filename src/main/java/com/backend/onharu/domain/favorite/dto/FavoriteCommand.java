package com.backend.onharu.domain.favorite.dto;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.store.model.Store;

/**
 * 찜하기 관련 Command DTO
 */
public class FavoriteCommand {

    /**
     * 찜하기 생성(등록) Command
     */
    public record CreateFavoriteCommand(
            Child child,
            Store store
    ) {
    }

    /**
     * 찜하기 취소(삭제) Command
     */
    public record DeleteFavoriteCommand(
            Favorite favorite
    ) {
    }

    /**
     * 찜하기 토글 Command
     *
     * @param childId 아동 ID
     * @param storeId 가게 ID
     */
    public record ToggleFavoriteCommand(
            Long childId,
            Long storeId
    ) {
    }
}
