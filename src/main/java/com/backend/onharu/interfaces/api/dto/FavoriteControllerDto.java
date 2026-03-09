package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.favorite.model.Favorite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 찜하기 API 에서 사용되는 DTO 입니다.
 */
public class FavoriteControllerDto {

    public record FavoriteToggleResponse(
            @Schema(name = "찜등록/찜취소 여부", description = "true: 찜등록, false: 찜취소")
            boolean isFavorite
    ) {
    }

    public record GetMyFavoriteListResponse(
            @Schema(description = "찜하기 목록")
            List<FavoriteResponse> favorites,

            @Schema(description = "전체 갯수")
            Long totalCount,

            @Schema(description = "현재 페이지 번호")
            Integer currentPage,

            @Schema(description = "전체 페이지 수")
            Integer totalPages,

            @Schema(description = "페이지당 항목 수")
            Integer perPage
    ) {
    }

    public record FavoriteResponse(
            @Schema(description = "찜하기 ID", example = "1")
            Long id,

            @Schema(description = "아이 ID", example = "10")
            Long childId,

            @Schema(description = "가게 ID", example = "200")
            Long storeId
    ) {
        public FavoriteResponse(Favorite favorite) {
            this(
                    favorite.getId(),
                    favorite.getChild().getId(),
                    favorite.getStore().getId()
            );
        }
    }
}
