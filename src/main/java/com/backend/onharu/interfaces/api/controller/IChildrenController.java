package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetCardResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetCertificateResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.GetLikedStoreListResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.IssueCardRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.IssueCardResponse;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.UpdateCardRequest;
import com.backend.onharu.interfaces.api.dto.ChildControllerDto.UpdateCertificateRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Children", description = "결식 아동 API")
public interface IChildrenController {

    @Operation(summary = "결식 아동 카드 등록", description = "결식 아동 카드를 등록합니다.")
    ResponseEntity<ResponseDTO<IssueCardResponse>> issueCard(
            @Schema(description = "카드 등록 요청")
            IssueCardRequest request
    );

    @Operation(summary = "결식 아동 카드 수정", description = "카드 정보를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateCard(
            @Schema(description = "카드 ID", example = "1")
            Long cardId,
            @Schema(description = "카드 수정 요청")
            UpdateCardRequest request
    );

    @Operation(summary = "결식 아동 카드 삭제", description = "카드를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteCard(
            @Schema(description = "카드 ID", example = "1")
            Long cardId
    );

    @Operation(summary = "결식 아동 카드 재발급 요청", description = "카드 재발급을 요청합니다.")
    ResponseEntity<ResponseDTO<Void>> reissueCard(
            @Schema(description = "카드 ID", example = "1")
            Long cardId
    );

    @Operation(summary = "결식 아동 카드 조회", description = "카드 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetCardResponse>> getMyCard(
            @Schema(description = "카드 ID", example = "1")
            Long cardId
    );

    @Operation(summary = "결식 아동 증명서 등록", description = "증명서를 등록합니다.")
    ResponseEntity<ResponseDTO<Void>> uploadCertificate(
            @Schema(description = "증명서 등록 요청")
            UpdateCertificateRequest request,
            @Schema(description = "증명서 파일")
            MultipartFile file
    );

    @Operation(summary = "결식 아동 증명서 수정", description = "증명서를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateMyCertificate(
            @Schema(description = "증명서 ID", example = "1")
            Long certificateId,
            @Schema(description = "증명서 수정 요청")
            UpdateCertificateRequest request,
            @Schema(description = "증명서 파일")
            MultipartFile file
    );

    @Operation(summary = "결식 아동 증명서 삭제", description = "증명서를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> removeMyCertificate(
            @Schema(description = "증명서 ID", example = "1")
            Long certificateId
    );

    @Operation(summary = "결식 아동 증명서 조회", description = "증명서를 조회합니다.")
    ResponseEntity<ResponseDTO<GetCertificateResponse>> getMyCertificate(
            @Schema(description = "증명서 ID", example = "1")
            Long certificateId
    );

    @Operation(summary = "관심 가게 목록 조회", description = "관심 가게 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetLikedStoreListResponse>> getFavoriteStores();
}
