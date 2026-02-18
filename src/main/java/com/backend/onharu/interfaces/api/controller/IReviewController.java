package com.backend.onharu.interfaces.api.controller;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.ReviewControllerDto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

@Tag(name = "Review", description = "리뷰 API")
public interface IReviewController {

    /**
     * 감사 리뷰 작성
     * <p>
     * POST /api/reviews/stores/{storeId}
     * 아동이 예약해서 이용한 가게에 감사 리뷰를 작성합니다. 가게 ID, 예약 ID, 리뷰 내용을 함께 받습니다.
     */
    @Operation(summary = "감사 리뷰 작성", description = "가게에 대한 감사 리뷰를 작성합니다.")
    ResponseEntity<ResponseDTO<WriteReviewResponse>> writeReview(
            @Parameter(description = "가게 ID", example = "10", required = true)
            @PathVariable Long storeId,

            @RequestBody(
                    description = "감사 리뷰 작성",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WriteReviewRequest.class),
                            examples = @ExampleObject(
                                    name = "감사 리뷰 작성 예시",
                                    value = """
                                            {
                                              "reservationId": 300,
                                              "content": "정말 따뜻한 마음으로 서비스를 제공해주셔서 감사합니다!"
                                            }
                                            """
                            )
                    )
            )
            WriteReviewRequest request
    );

    /**
     * 감사 리뷰 목록 조회
     * GET /api/reviews
     */
    @Operation(summary = "감사 리뷰 목록 조회", description = "전체 감사 리뷰 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetReviewListResponse>> getAllReviews(
            @Schema(description = "감사 리뷰 목록 조회 요청")
            @ParameterObject
            @ModelAttribute GetReviewsRequest request
    );

    /**
     * 감사 리뷰 상세 조회(페이징)
     * GET /api/reviews/stores/{storeId}
     */
    @Operation(summary = "감사 리뷰 상세 조회", description = "가게의 감사 리뷰를 조회합니다.")
    ResponseEntity<ResponseDTO<GetReviewDetailResponse>> getStoreReviews(
            @Parameter(description = "가게 ID", example = "1", required = true)
            @PathVariable
            Long storeId,

            @Schema(description = "감사 리뷰 목록 조회 요청")
            @ParameterObject
            GetReviewsRequest request
    );

    /**
     * 내가(아동이) 작성한 리뷰 목록 조회(페이징)
     * GET /api/reviews/my
     */
    @Operation(summary = "내가 작성한 리뷰 목록 조회", description = "내가 작성한 리뷰 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetMyReviewListResponse>> getMyReviews(
            @Schema(description = "감사 리뷰 목록 조회 요청")
            @ParameterObject
            GetReviewsRequest request
    );

    /**
     * 리뷰 삭제
     * DELETE /api/reviews/{reviewId}
     */
    @Operation(summary = "리뷰 삭제", description = "리뷰를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteMyReview(
            @Parameter(description = "삭제할 리뷰 ID", example = "1", required = true)
            @PathVariable Long reviewId
    );
}
