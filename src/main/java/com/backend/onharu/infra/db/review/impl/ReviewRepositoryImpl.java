package com.backend.onharu.infra.db.review.impl;

import com.backend.onharu.domain.review.model.Review;
import com.backend.onharu.domain.review.repository.ReviewRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.review.ReviewJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.backend.onharu.domain.review.dto.ReviewRepositoryParam.*;
import static com.backend.onharu.domain.support.error.ErrorType.Review.REVIEW_NOT_FOUND;

/**
 * 도메인 Review 의 Repository 구현체 입니다.
 */
@Repository
@RequiredArgsConstructor
public class ReviewRepositoryImpl implements ReviewRepository {

    private final ReviewJpaRepository reviewJpaRepository;

    @Override
    public Review save(Review review) {
        return reviewJpaRepository.save(review);
    }

    @Override
    public void delete(Review review) {
        reviewJpaRepository.delete(review);
    }

    @Override
    public Page<Review> findAll(Pageable pageable) {
        return reviewJpaRepository.findAll(pageable);
    }

    @Override
    public Review getReviewById(GetReviewByIdParam param) {
        return reviewJpaRepository.findById(param.reviewId())
                .orElseThrow(() -> new CoreException(REVIEW_NOT_FOUND));
    }

    @Override
    public List<Review> findAllByChildId(FindAllByChildIdParam param) {
        return reviewJpaRepository.findAllByChild_Id(param.childId());
    }

    @Override
    public Page<Review> findByChildId(FindAllByChildIdParam param, Pageable pageable) {
        return reviewJpaRepository.findByChild_Id(param.childId(), pageable);
    }

    @Override
    public List<Review> findAllByStoreId(FindAllByStoreIdParam param) {
        return reviewJpaRepository.findAllByStore_Id(param.storeId());
    }

    @Override
    public Page<Review> findByStoreId(FindAllByStoreIdParam param, Pageable pageable) {
        return reviewJpaRepository.findByStore_Id(param.storeId(), pageable);
    }

    @Override
    public void updateReview(UpdateReviewParam param) {
        reviewJpaRepository.updateReview(param.reviewId(), param.content());
    }
}
