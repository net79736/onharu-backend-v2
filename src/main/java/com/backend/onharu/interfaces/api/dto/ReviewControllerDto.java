package com.backend.onharu.interfaces.api.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class ReviewControllerDto {

    public record WriteReviewRequest(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId,

            @Schema(description = "리뷰 내용", example = "정말 따뜻한 마음으로 식사를 제공해주셔서 감사합니다!")
            String content
    ) {
    }

    public record WriteReviewResponse(
            @Schema(description = "리뷰 ID", example = "1")
            Long reviewId
    ) {
    }

    public record GetReviewListResponse(
            List<ReviewResponse> reviews
    ) {
    }

    public record GetReviewDetailResponse(
            List<ReviewResponse> reviews
    ) {
    }

    public record GetMyReviewListResponse(
            List<ReviewResponse> reviews
    ) {
    }

    public record ReviewResponse(
            @Schema(description = "리뷰 ID", example = "1")
            Long id,

            @Schema(description = "아이 ID", example = "1")
            Long childId,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "예약 ID", example = "1")
            Long reservationId,

            @Schema(description = "가게 이름", example = "따뜻한 식당")
            String name,

            @Schema(description = "리뷰 내용", example = "정말 따뜻한 마음으로 식사를 제공해주셔서 감사합니다!")
            String content
    ) {
    }
}
