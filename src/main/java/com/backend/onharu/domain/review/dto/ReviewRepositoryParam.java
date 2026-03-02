package com.backend.onharu.domain.review.dto;

/**
 * 리뷰 Repository 에 사용될 파마리터 DTO 입니다.
 */
public class ReviewRepositoryParam {

    /**
     * 리뷰 단일 조회 Param
     *
     * @param reviewId 리뷰 ID
     */
    public record GetReviewByIdParam(
            Long reviewId
    ) {
    }

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회 Param
     *
     * @param childId 아동 ID
     */
    public record FindAllByChildIdParam(
            Long childId
    ) {
    }

    /**
     * 특정 가게에 달린 리뷰 목록 조회 Param
     *
     * @param storeId 가게 ID
     */
    public record FindAllByStoreIdParam(
            Long storeId
    ) {
    }

    /**
     * 리뷰 수정 Param
     *
     * @param reviewId 리뷰 ID
     * @param content  수정할 내용
     */
    public record UpdateReviewParam(
            Long reviewId,
            String content
    ) {
    }
}
