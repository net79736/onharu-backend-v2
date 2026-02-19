package com.backend.onharu.interfaces.api.controller;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.CreateFavoriteResponse;
import com.backend.onharu.interfaces.api.dto.FavoriteControllerDto.GetMyFavoriteListResponse;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;

import static com.backend.onharu.interfaces.api.dto.ReviewControllerDto.*;

/**
 * 찜하기 API 설명 입니다.
 */
@Tag(name = "Favorite", description = "찜하기 API")
public interface IFavoriteController {

    /**
     * 찜등록(생성)
     * <p>
     * POST /favorites/stores/{storeId}
     * 특정 가게에 대한 찜을 등록합니다.
     * </p>
     */
    @Operation(summary = "찜하기 등록", description = "특정 가게를 찜합니다. 가게 ID 를 받습니다.")
    ResponseEntity<ResponseDTO<CreateFavoriteResponse>> createFavorite(
            @Schema(description = "가게 ID", example = "1")
            Long storeId
    );

    /**
     * 내 찜목록 조회
     * <p>
     * GET /favorites
     * 내가 작성한 찜목록을 조회합니다.
     * </p>
     */
    @Operation(summary = "내가 찜한 가게 목록 조회", description = "내가 찜한 가게목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetMyFavoriteListResponse>> getMyFavorite(
            @ParameterObject
            @ModelAttribute GetReviewsRequest request
    );

    /**
     * 내가 등록한 찜하기 취소
     * <p>
     * DELETE /favorites/{favoriteId}
     * 특정 찜하기를 삭제(취소)합니다.
     * </p>
     */
    @Operation(summary = "찜하기 취소", description = "찜한 가게를 취소합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteFavorite(
            @Schema(description = "찜하기 ID", example = "1")
            Long favoriteId
    );
}
