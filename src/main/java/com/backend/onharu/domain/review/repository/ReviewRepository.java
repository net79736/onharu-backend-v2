package com.backend.onharu.domain.review.repository;

import com.backend.onharu.domain.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.backend.onharu.domain.review.dto.ReviewRepositoryParam.*;

/**
 * 리뷰 Repository 인터페이스 입니다.
 */
public interface ReviewRepository {

    /**
     * 리뷰 생성(저장)
     */
    Review save(Review review);

    /**
     * 리뷰 삭제
     */
    void delete(Review review);

    /**
     * 리뷰 전체 목록 조회
     */
    List<Review> findAll();

    /**
     * 리뷰 단건 조회
     */
    Review getReviewById(GetReviewByIdParam param);

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회
     */
    List<Review> findAllByChildId(FindAllByChildIdParam param);

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회(페이징)
     */
    Page<Review> findByChildId(FindAllByChildIdParam param, Pageable pageable);

    /**
     * 특정 가게에 달린 리뷰 목록 조회
     */
    List<Review> findAllByStoreId(FindAllByStoreIdParam param);

    /**
     * 특정 가게에 달린 리뷰 목록 조회(페이징)
     */
    Page<Review> findByStoreId(FindAllByStoreIdParam param, Pageable pageable);

    /**
     * 리뷰 수정
     */
    void updateReview(UpdateReviewParam param);
}
