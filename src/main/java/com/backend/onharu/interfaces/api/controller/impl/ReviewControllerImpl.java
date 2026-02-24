package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.ReviewFacade;
import com.backend.onharu.domain.review.dto.ReviewCommand.CreateReviewCommand;
import com.backend.onharu.domain.review.model.Review;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.common.util.PageableUtil;
import com.backend.onharu.interfaces.api.controller.IReviewController;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.*;
import com.backend.onharu.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.backend.onharu.domain.review.dto.ReviewCommand.DeleteReviewCommand;
import static com.backend.onharu.domain.review.dto.ReviewQuery.FindAllByChildIdQuery;
import static com.backend.onharu.domain.review.dto.ReviewQuery.findAllByStoreIdQuery;

/**
 * 리뷰 관련 API를 제공하는 컨트롤러 구현체입니다.
 * <p>
 * 감사 리뷰 작성, 조회, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewControllerImpl implements IReviewController {

    private final ReviewFacade reviewFacade;

    /**
     * 감사 리뷰 작성
     * <p>
     * POST /api/reviews/stores/{storeId}
     * 특정 가게에 대한 감사 리뷰를 작성합니다.
     *
     * @param storeId 가게 ID
     * @param request 리뷰 작성 요청
     * @return 생성된 리뷰 정보
     */
    @Override
    @PostMapping("/stores/{storeId}")
    public ResponseEntity<ResponseDTO<WriteReviewResponse>> writeReview(
            @PathVariable("storeId") Long storeId,
            @RequestBody WriteReviewRequest request
    ) {
        log.info("감사 리뷰 작성 요청: storeId={}, request={}", storeId, request);

        Long childId = SecurityUtils.getCurrentUserId(); // 현재 인증된 아동 ID

        Review review = reviewFacade.createReview(
                new CreateReviewCommand(
                        childId,
                        storeId,
                        request.reservationId(),
                        request.content())
        ); // 리뷰 생성(저장)

        WriteReviewResponse response = new WriteReviewResponse(review.getId()); // 응답 생성

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
    }

    /**
     * 감사 리뷰 목록 조회
     * <p>
     * GET /api/reviews
     * 전체 감사 리뷰 목록을 조회합니다.
     *
     * @return 리뷰 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ResponseDTO<GetReviewListResponse>> getAllReviews(
            @ParameterObject
            @ModelAttribute GetReviewsRequest request
    ) {
        log.info("감사 리뷰 목록 조회 요청");

        Pageable pageable = PageableUtil.ofOneBased(
                request.pageNum(),
                request.perPage(),
                request.sortField(),
                request.sortDirection()
        ); // 페이징 정보

        Page<Review> reviews = reviewFacade.findAll(pageable); // 전체 리뷰 목록 조회

        Page<ReviewResponse> reviewResponses = reviews.map(
                review -> new ReviewResponse(
                        review.getId(),
                        review.getChild().getId(),
                        review.getStore().getId(),
                        review.getReservation().getId(),
                        review.getStore().getName(),
                        review.getContent(),
                        review.getCreatedAt().toLocalDate()
                )); // 응답을 담을 ReviewResponse 목록 생성

        GetReviewListResponse response = new GetReviewListResponse(
                reviewResponses.getContent(),
                reviews.getTotalElements(),
                reviewResponses.getNumber() + 1,
                reviewResponses.getTotalPages(),
                reviewResponses.getSize()
        ); // 감사 리뷰 목록 조회 응답 생성

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 감사 리뷰 상세 조회
     * <p>
     * GET /api/reviews/stores/{storeId}
     * 특정 가게의 감사 리뷰를 조회합니다.
     *
     * @param storeId 가게 ID
     * @return 가게의 리뷰 목록
     */
    @Override
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ResponseDTO<GetReviewDetailResponse>> getStoreReviews(
            @PathVariable("storeId") Long storeId,
            @ParameterObject
            @ModelAttribute GetReviewsRequest request
    ) {
        log.info("감사 리뷰 상세 조회 요청: storeId={}", storeId);

        Pageable pageable = PageableUtil.ofOneBased(
                request.pageNum(),
                request.perPage(),
                request.sortField(),
                request.sortDirection()
        ); // 페이징 정보

        Page<Review> reviews = reviewFacade.findByStoreId(
                new findAllByStoreIdQuery(storeId),
                pageable
        ); // 특정 가게에 달린 리뷰 목록 조회

        Page<ReviewResponse> reviewResponses = reviews.map(review ->
                new ReviewResponse(
                        review.getId(),
                        review.getChild().getId(),
                        review.getStore().getId(),
                        review.getReservation().getId(),
                        review.getStore().getName(),
                        review.getContent(),
                        review.getCreatedAt().toLocalDate()
                )); // 응답을 담을 ReviewResponse 목록 생성

        GetReviewDetailResponse response = new GetReviewDetailResponse(
                reviewResponses.getContent(),
                reviews.getTotalElements(),
                reviewResponses.getNumber() + 1,
                reviewResponses.getTotalPages(),
                reviewResponses.getSize()
        ); // 감사 리뷰 상세 조회 응답 생성

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 내가 작성한 리뷰 목록 조회
     * <p>
     * GET /api/reviews/my
     * 내가 작성한 리뷰 목록을 조회합니다.
     *
     * @return 내가 작성한 리뷰 목록
     */
    @Override
    @GetMapping("/my")
    public ResponseEntity<ResponseDTO<GetMyReviewListResponse>> getMyReviews(
            @ParameterObject
            @ModelAttribute GetReviewsRequest request
    ) {
        log.info("내가 작성한 리뷰 목록 조회 요청");

        Long childId = SecurityUtils.getCurrentUserId(); // 현재 인증된 아동 ID 획득

        Pageable pageable = PageableUtil.ofOneBased(
                request.pageNum(),
                request.perPage(),
                request.sortField(),
                request.sortDirection()
        ); // 페이징 정보

        Page<Review> reviews = reviewFacade.findByChildId(
                new FindAllByChildIdQuery(childId),
                pageable
        ); // 내가(아동이) 작성한 리뷰 목록 조회

        Page<ReviewResponse> reviewResponses = reviews.map(review ->
                new ReviewResponse(
                        review.getId(),
                        review.getChild().getId(),
                        review.getStore().getId(),
                        review.getReservation().getId(),
                        review.getStore().getName(),
                        review.getContent(),
                        review.getCreatedAt().toLocalDate()
                )); // 응답을 담을 ReviewResponse 목록 생성

        GetMyReviewListResponse response = new GetMyReviewListResponse(
                reviewResponses.getContent(),
                reviews.getTotalElements(),
                reviewResponses.getNumber() + 1,
                reviewResponses.getTotalPages(),
                reviewResponses.getSize()
        ); // 내가 작성한 리뷰 목록 조회 응답 생성

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 리뷰 삭제
     * <p>
     * DELETE /api/reviews/{reviewId}
     * 특정 리뷰를 삭제합니다.
     *
     * @param reviewId 리뷰 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseDTO<Void>> deleteMyReview(
            @PathVariable("reviewId") Long reviewId
    ) {
        log.info("리뷰 삭제 요청: reviewId={}", reviewId);

        reviewFacade.deleteReview(new DeleteReviewCommand(reviewId)); // 리뷰 삭제

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}