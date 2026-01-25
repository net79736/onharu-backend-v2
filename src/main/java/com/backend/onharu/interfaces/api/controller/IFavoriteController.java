package com.backend.onharu.interfaces.api.controller;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.CreateFavoriteResponse;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.GetMyFavoriteListResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Favorite", description = "찜하기 API")
public interface IFavoriteController {

    @Operation(summary = "찜하기 등록", description = "특정 가게를 찜합니다.")
    ResponseEntity<ResponseDTO<CreateFavoriteResponse>> createFavorite(
            @Schema(description = "가게 ID", example = "1")
            Long storeId
    );

    @Operation(summary = "내가 찜한 가게 목록 조회", description = "내가 찜한 가게목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetMyFavoriteListResponse>> getMyFavorite();

    @Operation(summary = "찜하기 취소", description = "찜한 가게를 취소합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteFavorite(
            @Schema(description = "찜하기 ID", example = "1")
            Long favoriteId
    );
}
