package com.backend.onharu.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

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
            Long storeId,

            @Schema(defaultValue = "가게명", example = "따뜻한 가게")
            String storeName,

            @Schema(defaultValue = "가게 대표 이미지")
            List<String> image,

            @Schema(defaultValue = "가게 주소", example = "서울시 강남구 테헤란로 123")
            String address,

            @Schema(defaultValue = "나눔 진행 여부", example = "true")
            boolean isShare
    ) {
    }
}
