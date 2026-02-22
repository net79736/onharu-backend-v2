package com.backend.onharu.infra.db.review;

import com.backend.onharu.domain.review.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Review 의 JPA Repository 입니다.
 */
public interface ReviewJpaRepository extends JpaRepository<Review, Long> {

    /**
     * 리뷰 ID 로 단일 리뷰 조회
     *
     * @param id 리뷰 ID
     * @return 리뷰 엔티티
     */
    Optional<Review> findById(Long id);

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회
     *
     * @param childId 아동 ID
     * @return 조회된 리뷰 리스트
     */
    List<Review> findAllByChild_Id(Long childId);

    /**
     * 본인(아동)이 작성한 리뷰 목록 조회(페이징)
     *
     * @param childId 아동 ID
     * @param pageable 페이징 설정
     * @return 페이징된 리뷰 목록
     */
    Page<Review> findByChild_Id(Long childId, Pageable pageable);

    /**
     * 특정 가게에 달린 리뷰 목록 조회
     *
     * @param storeId 가게 ID
     * @return 조회된 리뷰 리스트
     */
    List<Review> findAllByStore_Id(Long storeId);

    /**
     * 특정 가게에 달린 리뷰 목록 조회(페이징)
     *
     * @param storeId 가게 ID
     * @param pageable 페이징 설정
     * @return 페이징된 리뷰 목록
     */
    Page<Review> findByStore_Id(Long storeId, Pageable pageable);

    /**
     * 리뷰 수정
     *
     * @param id 리뷰 ID
     * @param content 수정할 리뷰 내용
     */
    @Modifying
    @Query("UPDATE Review r SET r.content = :content WHERE r.id = :id")
    void updateReview(Long id, String content);
}
