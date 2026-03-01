package com.backend.onharu.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

/**
 * 감사 리뷰 API 에 사용될 DTO 입니다.
 */
public class ReviewControllerDto {

    /**
     * 감사 리뷰 작성 요청
     * @param reservationId 예약 ID
     * @param content 리뷰 내용
     */
    public record WriteReviewRequest(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId,

            @Schema(description = "리뷰 내용", example = "정말 따뜻한 마음으로 식사를 제공해주셔서 감사합니다!")
            String content
    ) {
    }

    /**
     * 감사 리뷰 작성 응답
     * @param reviewId 작성된 리뷰 ID
     */
    public record WriteReviewResponse(
            @Schema(description = "리뷰 ID", example = "1")
            Long reviewId
    ) {
    }

    /**
     * 감사 리뷰 목록 조회 요청 (페이징)
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

    /**
     * 감사 리뷰 목록 조회 응답 (페이징)
     * @param reviews
     * @param totalCount
     * @param currentPage
     * @param totalPages
     * @param perPage
     */
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

    /**
     * 감사 리뷰 상세 정보
     * @param reviews 감사 리뷰 목록
     * @param totalCount 전체 항목 갯수
     * @param currentPage 현재 페이지 번호
     * @param totalPages 전체 페이지 수
     * @param perPage 페이지당 항목 수
     */
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

    /**
     * 내가 작성한 감사리뷰 목록 응답
     * @param reviews 감사 리뷰 목록
     * @param totalCount 전체 항목 갯수
     * @param currentPage 현재 페이지 번호
     * @param totalPages 전체 페이지 수
     * @param perPage 페이지당 항목 수
     */
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

    /**
     * 감사 리뷰 응답 정보
     * @param id 리뷰 ID
     * @param childId 아동 ID
     * @param storeId 가게 ID
     * @param reservationId  예약 ID
     * @param name 리뷰대상 가게 이름
     * @param content 리뷰 내용
     * @param createAt 리뷰 작성 날짜
     */
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
            String content,

            @Schema(defaultValue = "리뷰 작성 날짜", example = "2026-02-24")
            LocalDate createAt
    ) {
    }
}
