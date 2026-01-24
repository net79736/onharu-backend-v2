package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.GetMyReviewListResponse;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.GetReviewDetailResponse;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.GetReviewListResponse;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.WriteReviewRequest;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.WriteReviewResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Review", description = "리뷰 API")
public interface IReviewController {

    @Operation(summary = "감사 리뷰 작성", description = "가게에 대한 감사 리뷰를 작성합니다.")
    ResponseEntity<ResponseDTO<WriteReviewResponse>> writeReview(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @Schema(description = "리뷰 작성 요청")
            WriteReviewRequest request
    );

    @Operation(summary = "감사 리뷰 목록 조회", description = "전체 감사 리뷰 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetReviewListResponse>> getAllReviews();

    @Operation(summary = "감사 리뷰 상세 조회", description = "가게의 감사 리뷰를 조회합니다.")
    ResponseEntity<ResponseDTO<GetReviewDetailResponse>> getStoreReviews(
            @Schema(description = "가게 ID", example = "1")
            Long storeId
    );

    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "내가 작성한 리뷰 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetMyReviewListResponse>> getMyReviews();

    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteMyReview(
            @Schema(description = "리뷰 ID", example = "1")
            Long reviewId
    );
}
