package com.backend.onharu.domain.review.service;

import com.backend.onharu.domain.review.model.Review;
import com.backend.onharu.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.backend.onharu.domain.review.dto.ReviewQuery.*;
import static com.backend.onharu.domain.review.dto.ReviewRepositoryParam.*;

@Service
@Transactional
@RequiredArgsConstructor
public class ReviewQueryService {

    private final ReviewRepository reviewRepository;

    /**
     * 리뷰 단건 조회
     *
     * @param query 리뷰 ID 를 포함된 query
     * @return 조회된 리뷰 정보 (없으면 예외 발생)
     */
    public Review getReviewById(GetReviewByIdQuery query) {
        return reviewRepository.getReviewById(
                new GetReviewByIdParam(query.reviewId())
        );
    }

    /**
     * 전체 리뷰 목록 조회
     * @return 조회된 리뷰 목록
     */
    public List<Review> findAll() {
        return reviewRepository.findAll();
    }

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회
     *
     * @param query 아동 ID 를 포함된 query
     * @return 조회된 리뷰 목록
     */
    public List<Review> findAllByChildId(FindAllByChildIdQuery query) {
        return reviewRepository.findAllByChildId(
                new FindAllByChildIdParam(query.childId()
                )
        );
    }

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회(페이징)
     *
     * @param query    아동 ID 를 포함된 query
     * @param pageable 페이징 정보
     * @return 아동이 작성한 리뷰 목록
     */
    public Page<Review> findByChildId(FindAllByChildIdQuery query, Pageable pageable) {
        return reviewRepository.findByChildId(
                new FindAllByChildIdParam(query.childId()),
                pageable
        );
    }

    /**
     * 특정 가게에 달린 리뷰 목록 조회
     *
     * @param query 가게 ID 를 포함된 query
     * @return 가게에 달린 리뷰 목록
     */
    public List<Review> findAllByStoreId(findAllByStoreIdQuery query) {
        return reviewRepository.findAllByStoreId(
                new FindAllByStoreIdParam(query.storeId())
        );
    }

    /**
     * 특정 가게에 달린 리뷰 목록 조회(페이징)
     *
     * @param query    가게 ID 를 포함한 query
     * @param pageable 페이징 정보
     * @return 가게에 달린 리뷰 목록
     */
    public Page<Review> findByStoreId(findAllByStoreIdQuery query, Pageable pageable) {
        return reviewRepository.findByStoreId(
                new FindAllByStoreIdParam(query.storeId()),
                pageable
        );
    }
}
