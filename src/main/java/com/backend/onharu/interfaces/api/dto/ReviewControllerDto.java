package com.backend.onharu.interfaces.api.dto;

import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

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

    /**
     * 감사 리뷰 목록 조회 요청
     * @param pageNum 페이지 번호
     * @param perPage 항목 수
     * @param sortField 정렬 기준
     * @param sortDirection 정렬 방향(오름차순, 내림차순)
     */
    public record GetReviewsRequest(
            @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
            Integer pageNum,

            @Schema(description = "페이지당 항목 수", example = "10")
            Integer perPage,

            @Schema(description = "정렬 기준", example = "id", allowableValues = {"id"})
            String sortField,

            @Schema(description = "정렬 방향", example = "desc", allowableValues = {"asc", "desc"})
            String sortDirection
    ) {
    }

    public record GetReviewListResponse(
            @Schema(description = "감사 리뷰 목록")
            List<ReviewResponse> reviews,

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

    public record GetReviewDetailResponse(
            @Schema(description = "감사 리뷰 목록")
            List<ReviewResponse> reviews,

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

    public record GetMyReviewListResponse(
            @Schema(description = "감사 리뷰 목록")
            List<ReviewResponse> reviews,

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
