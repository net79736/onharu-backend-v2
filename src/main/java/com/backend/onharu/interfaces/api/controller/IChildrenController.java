package com.backend.onharu.interfaces.api.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.BookStoreRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.BookStoreResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.CancelReservationRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetCardResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetCertificateResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetMyBookingDetailResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetMyBookingListResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetMyBookingsRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.IssueCardRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.IssueCardResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.UpdateCardRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.UpdateCertificateRequest;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Children", description = "결식 아동 API")
public interface IChildrenController {

    @Hidden
    @Operation(summary = "결식 아동 카드 등록", description = "결식 아동 카드를 등록합니다.")
    ResponseEntity<ResponseDTO<IssueCardResponse>> issueCard(
            @Schema(description = "카드 등록 요청")
            IssueCardRequest request
    );

    @Hidden
    @Operation(summary = "결식 아동 카드 수정", description = "카드 정보를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateCard(
            @Schema(description = "카드 ID", example = "1")
            Long cardId,
            @Schema(description = "카드 수정 요청")
            UpdateCardRequest request
    );

    @Hidden
    @Operation(summary = "결식 아동 카드 삭제", description = "카드를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteCard(
            @Schema(description = "카드 ID", example = "1")
            Long cardId
    );

    @Hidden
    @Operation(summary = "결식 아동 카드 재발급 요청", description = "카드 재발급을 요청합니다.")
    ResponseEntity<ResponseDTO<Void>> reissueCard(
            @Schema(description = "카드 ID", example = "1")
            Long cardId
    );

    @Hidden
    @Operation(summary = "결식 아동 카드 조회", description = "카드 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetCardResponse>> getMyCard(
            @Schema(description = "카드 ID", example = "1")
            Long cardId
    );

    @Hidden
    @Operation(summary = "결식 아동 증명서 등록", description = "증명서를 등록합니다.")
    ResponseEntity<ResponseDTO<Void>> uploadCertificate(
            @Schema(description = "증명서 등록 요청")
            UpdateCertificateRequest request,
            @Schema(description = "증명서 파일")
            MultipartFile file
    );

    @Hidden
    @Operation(summary = "결식 아동 증명서 수정", description = "증명서를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateMyCertificate(
            @Schema(description = "증명서 ID", example = "1")
            Long certificateId,
            @Schema(description = "증명서 수정 요청")
            UpdateCertificateRequest request,
            @Schema(description = "증명서 파일")
            MultipartFile file
    );

    @Hidden
    @Operation(summary = "결식 아동 증명서 삭제", description = "증명서를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> removeMyCertificate(
            @Schema(description = "증명서 ID", example = "1")
            Long certificateId
    );

    @Hidden
    @Operation(summary = "결식 아동 증명서 조회", description = "증명서를 조회합니다.")
    ResponseEntity<ResponseDTO<GetCertificateResponse>> getMyCertificate(
            @Schema(description = "증명서 ID", example = "1")
            Long certificateId
    );

    @Operation(summary = "가게 예약 생성", description = "가게에 예약을 생성합니다.")
    ResponseEntity<ResponseDTO<BookStoreResponse>> bookStore(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @RequestBody(
                    description = "가게 예약 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = BookStoreRequest.class),
                            examples = @ExampleObject(
                                    name = "가게 예약 생성 예시",
                                    value = "{\n" +
                                            "  \"storeScheduleId\": 1,\n" +
                                            "  \"people\": 1\n" +
                                            "}"
                            )
                    )
            )
            BookStoreRequest request
    );

    @Operation(summary = "예약 취소", description = "기존 예약을 취소합니다.")
    ResponseEntity<ResponseDTO<Void>> cancelStore(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId,
            @RequestBody(
                description = "예약 취소 요청",
                required = true,
                content = @Content(
                        mediaType = "application/json",
                        schema = @Schema(implementation = CancelReservationRequest.class),
                        examples = @ExampleObject(
                                name = "예약 취소 예시",
                                value = "{\n" +
                                        "  \"cancelReason\": \"일정 변경으로 인한 취소\"\n" +
                                        "}"
                        )
                )
        )
            CancelReservationRequest request
    );

    @Operation(summary = "예약 신청 목록 조회", description = "내가 신청한 예약 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetMyBookingListResponse>> getMyBookings(
            @Schema(description = "내가 신청한 예약 목록 조회 요청")
            @ParameterObject GetMyBookingsRequest request
    );

    @Operation(summary = "예약 신청 상세 조회", description = "내가 신청한 특정 예약의 상세 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetMyBookingDetailResponse>> getMyBooking(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );
}
