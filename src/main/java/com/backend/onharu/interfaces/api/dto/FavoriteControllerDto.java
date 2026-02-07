package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.favorite.model.Favorite;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

/**
 * 찜하기 API 에서 사용되는 DTO 입니다.
 */
public class FavoriteControllerDto {

    public record CreateFavoriteResponse(
            @NotNull(message = "찜하기 ID 는 필수 입력 값 입니다.")
            @Schema(description = "찜하기 ID", example = "1")
            Long favoriteId
    ) {
    }

    public record GetMyFavoriteListResponse(
            @Schema(description = "찜하기 목록")
            List<FavoriteResponse> favorites
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

    @Schema(description = "찜하기 취소(삭졔) 요청")
    public record DeleteFavoriteRequest(
            @NotNull(message = "찜하기 ID 는 필수 입력 값 입니다.")
            @Schema(description = "찜하기 ID", example = "1")
            Long id,

            @NotNull(message = "아이 ID 는 필수 입력 값 입니다.")
            @Schema(description = "아이 ID", example = "10")
            Long childId
    ) {
    }
}
