package com.backend.onharu.interfaces.api.controller;

import java.util.List;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.CategoryResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreDetailByIdRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreDetailResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.SearchStoresRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.SearchStoresResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UploadStoresByExcelResponse;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.GetStoreSchedulesRequest;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.GetStoreSchedulesResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Store", description = "가게 API")
public interface IStoreController {

    @Operation(summary = "가게 상세 정보 조회", description = "가게의 상세 정보를 반환합니다.")
    ResponseEntity<ResponseDTO<GetStoreDetailResponse>> getStore(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @ParameterObject GetStoreDetailByIdRequest request
    );

    @Operation(
        summary = "가게 목록 조회", 
        description = "검색 및 페이징이 가능한 가게 목록을 반환합니다."
    )
    ResponseEntity<ResponseDTO<SearchStoresResponse>> searchStores(
        @ParameterObject SearchStoresRequest request
    );

    @Operation(summary = "가게 스케줄 조회", description = "가게의 예약 가능한 스케줄을 조회합니다.")
    ResponseEntity<ResponseDTO<GetStoreSchedulesResponse>> getStoreSchedules(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @ParameterObject GetStoreSchedulesRequest request
    );

    @Operation(summary = "가게 카테고리 정보 목록 반환", description = "가게 카테고리 정보 목록을 반환합니다.")
    ResponseEntity<ResponseDTO<List<CategoryResponse>>> getCategoryList();

    @Operation(summary = "엑셀 파일을 통해 가게 정보를 일괄 등록", description = "엑셀 파일을 통해 가게 정보를 일괄 등록합니다.")
    ResponseEntity<ResponseDTO<UploadStoresByExcelResponse>> uploadStoresByExcel(
            @RequestPart("file") MultipartFile file
    );
}
