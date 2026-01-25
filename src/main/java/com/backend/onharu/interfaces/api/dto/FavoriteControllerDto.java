package com.backend.onharu.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

public class FavoriteControllerDto {

    public record CreateFavoriteResponse(
            @Schema(description = "찜하기 ID", example = "1")
            Long favoriteId
    ) {
    }

    public record GetMyFavoriteListResponse(
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
    }
}
