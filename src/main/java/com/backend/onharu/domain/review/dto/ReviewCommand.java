package com.backend.onharu.domain.review.dto;

/**
 * 리뷰 서비스에 사용될 Command DTO 입니다.
 */
public class ReviewCommand {

    /**
     * 리뷰 생성 Command
     * @param childId 아동 ID
     * @param storeId 가게 ID
     * @param reservationId 예약 ID
     * @param content 리뷰 내용
     */
    public record CreateReviewCommand(
            Long childId,
            Long storeId,
            Long reservationId,
            String content
    ) {
        public CreateReviewCommand {
        }
    }

    /**
     * 리뷰 수정 Command
     * @param reviewId 리뷰 ID
     * @param content 수정할 리뷰 내용
     */
    public record UpdateReviewCommand(
            Long reviewId,
            String content
    ) {
        public UpdateReviewCommand {
        }
    }

    /**
     * 리뷰 삭제 Command
     * @param reviewId 리뷰 ID
     */
    public record DeleteReviewCommand(
            Long reviewId
    ) {
        public DeleteReviewCommand {
        }
    }
}
