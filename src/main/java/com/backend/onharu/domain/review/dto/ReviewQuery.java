package com.backend.onharu.domain.review.dto;

import java.util.List;

/**
 * 리뷰 조회 서비스에 사용될 Query DTO 입니다.
 */
public class ReviewQuery {

    /**
     * 리뷰 단일 조회 query
     * @param reviewId 리뷰 ID
     */
    public record GetReviewByIdQuery(
            Long reviewId
    ) {
    }

    /**
     * 특정 아동의 리뷰 목록 조회 query
     * @param childId 아동 ID
     */
    public record FindAllByChildIdQuery(
            Long childId
    ) {
    }

    /**
     * 특정 가게의 리뷰 목록 조회 query
     * @param storeId 가게 ID
     */
    public record findAllByStoreIdQuery(
            Long storeId
    ) {
    }

    /**
     * 예약 ID 목록에 대해 리뷰 작성 여부 조회 query
     * @param reservationIds 예약 ID 목록
     */
    public record FindReviewedReservationIdsQuery(
            List<Long> reservationIds
    ) {
    }
}
