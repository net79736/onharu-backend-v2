package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreDetailResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreListResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UpdateStoreRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Store", description = "가게 API")
public interface IStoreController {

    @Operation(summary = "가게 상세 정보 조회", description = "가게의 상세 정보를 반환합니다.")
    ResponseEntity<ResponseDTO<GetStoreDetailResponse>> getStore(
            @Schema(description = "가게 ID", example = "1")
            Long storeId
    );

    @Operation(summary = "가게 목록 조회", description = "가능한 가게 목록을 반환합니다.")
    ResponseEntity<ResponseDTO<GetStoreListResponse>> searchStores(
            @Schema(description = "위도", example = "37.5665")
            Double latitude,
            @Schema(description = "경도", example = "126.9780")
            Double longitude,
            @Schema(description = "반경(km)", example = "5")
            Double radius
    );

    @Operation(summary = "가게 정보 작성", description = "신규 가게 정보를 생성합니다.")
    ResponseEntity<ResponseDTO<OpenStoreResponse>> openStore(
            @Schema(description = "가게 정보 생성 요청")
            OpenStoreRequest request
    );

    @Operation(summary = "가게 정보 삭제", description = "가게 정보를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> closeStore(
            @Schema(description = "가게 ID", example = "1")
            Long storeId
    );

    @Operation(summary = "가게 정보 수정", description = "가게 정보를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateMyStore(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @Schema(description = "가게 정보 수정 요청")
            UpdateStoreRequest request
    );
}
