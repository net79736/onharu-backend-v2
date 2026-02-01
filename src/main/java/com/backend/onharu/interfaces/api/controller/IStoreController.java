package com.backend.onharu.interfaces.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.CategoryResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.GetStoreDetailResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.SearchStoresRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.SearchStoresResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UpdateStoreRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Store", description = "가게 API")
public interface IStoreController {

    @Operation(summary = "가게 상세 정보 조회", description = "가게의 상세 정보를 반환합니다.")
    ResponseEntity<ResponseDTO<GetStoreDetailResponse>> getStore(
            @Schema(description = "가게 ID", example = "1")
            Long storeId
    );

    @Operation(summary = "가게 목록 조회", description = "가능한 가게 목록을 반환합니다.")
    ResponseEntity<ResponseDTO<SearchStoresResponse>> searchStores(
            @Schema(description = "가게 목록 조회 요청")
            SearchStoresRequest request
    );

    @Operation(summary = "가게 정보 작성", description = "신규 가게 정보를 생성합니다.")
    ResponseEntity<ResponseDTO<OpenStoreResponse>> openStore(
            @RequestBody(
                    description = "가게 정보 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = OpenStoreRequest.class),
                            examples = @ExampleObject(
                                    name = "가게 정보 작성 예시",
                                    value = "{\n" +
                                            "  \"categoryId\": 1,\n" +
                                            "  \"name\": \"따뜻한 식당\",\n" +
                                            "  \"address\": \"서울시 강남구 테헤란로 123\",\n" +
                                            "  \"phone\": \"0212345678\",\n" +
                                            "  \"lat\": \"37.5665\",\n" +
                                            "  \"lng\": \"126.9780\",\n" +
                                            "  \"image\": \"https://onharu.com/images/store1.jpg\",\n" +
                                            "  \"intro\": \"따뜻한 한 끼 식사\",\n" +
                                            "  \"introduction\": \"따뜻한 마음으로 환영합니다!\",\n" +
                                            "  \"tagNames\": [\"한식\", \"점심식사\", \"따뜻한\"],\n" +
                                            "  \"businessHours\": [\n" +
                                            "    {\n" +
                                            "      \"businessDay\": \"2024-12-31\",\n" +
                                            "      \"openTime\": \"09:00\",\n" +
                                            "      \"closeTime\": \"22:00\"\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            )
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
            @RequestBody(
                    description = "가게 정보 수정 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateStoreRequest.class),
                            examples = @ExampleObject(
                                    name = "가게 정보 수정 예시",
                                    value = "{\n" +
                                            "  \"categoryId\": 1,\n" +
                                            "  \"image\": \"https://onharu.com/images/store1.jpg\",\n" +
                                            "  \"phone\": \"0212345678\",\n" +
                                            "  \"address\": \"서울시 강남구 테헤란로 123\",\n" +
                                            "  \"lat\": \"37.5665\",\n" +
                                            "  \"lng\": \"126.9780\",\n" +
                                            "  \"introduction\": \"따뜻한 한 끼 식사\",\n" +
                                            "  \"intro\": \"따뜻한 마음으로 환영합니다!\",\n" +
                                            "  \"isOpen\": true,\n" +
                                            "  \"tagNames\": [\"한식\", \"점심식사\", \"따뜻한\"],\n" +
                                            "  \"businessHours\": [\n" +
                                            "    {\n" +
                                            "      \"businessDay\": \"2024-12-31\",\n" +
                                            "      \"openTime\": \"09:00\",\n" +
                                            "      \"closeTime\": \"22:00\"\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"businessDay\": \"2025-01-01\",\n" +
                                            "      \"openTime\": \"10:00\",\n" +
                                            "      \"closeTime\": \"21:00\"\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            )
            UpdateStoreRequest request
    );

    @Operation(summary = "가게 카테고리 정보 목록 반환", description = "가게 카테고리 정보 목록을 반환합니다.")
    ResponseEntity<ResponseDTO<List<CategoryResponse>>> getCategoryList();
}
