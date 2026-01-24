package com.backend.onharu.interfaces.api.controller.impl;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IReviewController;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.GetMyReviewListResponse;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.GetReviewDetailResponse;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.GetReviewListResponse;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.WriteReviewRequest;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.WriteReviewResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 리뷰 관련 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 감사 리뷰 작성, 조회, 삭제 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewControllerImpl implements IReviewController {

    /**
     * 감사 리뷰 작성
     * 
     * POST /reviews/stores/{storeId}
     * 특정 가게에 대한 감사 리뷰를 작성합니다.
     *
     * @param storeId 가게 ID
     * @param request 리뷰 작성 요청
     * @return 생성된 리뷰 정보
     */
    @Override
    @PostMapping("/stores/{storeId}")
    public ResponseEntity<ResponseDTO<WriteReviewResponse>> writeReview(
            @PathVariable Long storeId,
            @RequestBody WriteReviewRequest request
    ) {
        log.info("감사 리뷰 작성 요청: storeId={}, request={}", storeId, request);
        
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(null));
    }

    /**
     * 감사 리뷰 목록 조회
     * 
     * GET /reviews
     * 전체 감사 리뷰 목록을 조회합니다.
     *
     * @return 리뷰 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ResponseDTO<GetReviewListResponse>> getAllReviews() {
        log.info("감사 리뷰 목록 조회 요청");
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 감사 리뷰 상세 조회
     * 
     * GET /reviews/stores/{storeId}
     * 특정 가게의 감사 리뷰를 조회합니다.
     *
     * @param storeId 가게 ID
     * @return 가게의 리뷰 목록
     */
    @Override
    @GetMapping("/stores/{storeId}")
    public ResponseEntity<ResponseDTO<GetReviewDetailResponse>> getStoreReviews(
            @PathVariable Long storeId
    ) {
        log.info("감사 리뷰 상세 조회 요청: storeId={}", storeId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 내가 작성한 리뷰 목록 조회
     * 
     * GET /reviews/my
     * 내가 작성한 리뷰 목록을 조회합니다.
     *
     * @return 내가 작성한 리뷰 목록
     */
    @Override
    @GetMapping("/my")
    public ResponseEntity<ResponseDTO<GetMyReviewListResponse>> getMyReviews() {
        log.info("내가 작성한 리뷰 목록 조회 요청");
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }

    /**
     * 리뷰 삭제
     * 
     * DELETE /reviews/{reviewId}
     * 특정 리뷰를 삭제합니다.
     *
     * @param reviewId 리뷰 ID
     * @return 삭제 결과
     */
    @Override
    @DeleteMapping("/{reviewId}")
    public ResponseEntity<ResponseDTO<Void>> deleteMyReview(
            @PathVariable Long reviewId
    ) {
        log.info("리뷰 삭제 요청: reviewId={}", reviewId);
        
        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(null));
    }
}