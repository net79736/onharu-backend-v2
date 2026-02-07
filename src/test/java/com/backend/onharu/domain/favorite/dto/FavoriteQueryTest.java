package com.backend.onharu.domain.favorite.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

/**
 * 찜하기 Query DTO 테스트 코드 입니다.
 */
class FavoriteQueryTest {

    @Test
    @DisplayName("찜하기 ID 로 단일 조회 쿼리 생성 테스트")
    void getFavoriteByIdQuery() {
        Long favoriteId = 1L;

        FavoriteQuery.GetFavoriteByIdQuery query = new FavoriteQuery.GetFavoriteByIdQuery(favoriteId);

        assertThat(query.favoriteId()).isEqualTo(favoriteId);
    }

    @Test
    @DisplayName("아동 ID 로 찜하기 목록 조회 생성 테스트")
    void findFavoritesByChildIdQuery() {
        Long childId = 20L;

        FavoriteQuery.FindFavoritesByChildIdQuery query = new FavoriteQuery.FindFavoritesByChildIdQuery(childId);

        assertThat(query.childId()).isEqualTo(childId);
    }
}