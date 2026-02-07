package com.backend.onharu.domain.favorite.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 찜하기 Command DTO 테스트 코드 입니다.
 */
class FavoriteCommandTest {

    @Test
    @DisplayName("찜하기 생성 Command 생성 테스트")
    void createFavoriteCommand() {
        Long childId = 1L;
        Long storeId = 20L;

        FavoriteCommand.CreateFavoriteCommand command = new FavoriteCommand.CreateFavoriteCommand(childId, storeId);

        assertThat(command.childId()).isEqualTo(childId);
        assertThat(command.storeId()).isEqualTo(storeId);
    }

    @Test
    @DisplayName("찜하기 취소 Command 생성 테스트")
    void deleteFavoriteCommand() {
        Long childId = 1L;
        Long favoriteId = 300L;

        FavoriteCommand.DeleteFavoriteCommand command = new FavoriteCommand.DeleteFavoriteCommand(childId, favoriteId);

        assertThat(command.childId()).isEqualTo(childId);
        assertThat(command.favoriteId()).isEqualTo(favoriteId);
    }
}