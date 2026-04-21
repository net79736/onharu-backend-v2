package com.backend.onharu.domain.review.service;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.review.model.Review;
import com.backend.onharu.domain.review.repository.ReviewRepository;
import com.backend.onharu.domain.store.model.Store;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.review.dto.ReviewCommand.*;
import static com.backend.onharu.domain.review.dto.ReviewRepositoryParam.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewCommandService {

    private final ReviewRepository reviewRepository;

    /**
     * 리뷰 생성
     *
     * 주의: 리뷰 생성시 Child, Store, Reservation 엔티티를 별도로 조회해야합니다.
     */
    public Review createReview(CreateReviewCommand command, Child child, Store store, Reservation reservation) {
        Review review = Review.builder()
                .child(child)
                .store(store)
                .reservation(reservation)
                .content(command.content())
                .build();

        return reviewRepository.save(review);
    }

    /**
     * 리뷰 수정
     */
    public void updateReview(UpdateReviewCommand command) {
        Review review = reviewRepository.getReviewById(
                new GetReviewByIdParam(
                        command.reviewId()
                )
        ); // 수정할 리뷰 조회

        reviewRepository.updateReview(
                new UpdateReviewParam(
                        command.reviewId(),
                        command.content()
                )
        ); // 리뷰 업데이트
    }

    /**
     * 리뷰 삭제
     */
    public void deleteReview(DeleteReviewCommand command) {
        Review review = reviewRepository.getReviewById(
                new GetReviewByIdParam(
                        command.reviewId()
                )
        ); // 삭제할 리뷰 조회

        reviewRepository.delete(review); // 리뷰 삭제
    }
}
