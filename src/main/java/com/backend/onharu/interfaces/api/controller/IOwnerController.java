package com.backend.onharu.interfaces.api.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CancelReservationRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CreateOwnerRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.CreateOwnerResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetMyStoresRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetMyStoresResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetOwnerResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingDetailResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingListResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingSummaryResponse;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingsRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.RemoveAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.SetAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateOwnerRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreRequest;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.OpenStoreResponse;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.UpdateStoreRequest;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Owner", description = "사업주 API")
public interface IOwnerController {

    @Hidden
    @Operation(summary = "사업자 정보 등록", description = "사업자 정보를 등록합니다.")
    ResponseEntity<ResponseDTO<CreateOwnerResponse>> registerBusiness(
            @Schema(description = "사업자 정보 등록 요청")
            CreateOwnerRequest request,
            @Schema(description = "사업자 등록 서류 파일")
            MultipartFile businessRegistrationFile
    );

    @Hidden
    @Operation(summary = "사업자 정보 수정", description = "사업자 정보를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateBusiness(
            @Schema(description = "사업자 ID", example = "1")
            Long ownerId,
            @Schema(description = "사업자 정보 수정 요청")
            UpdateOwnerRequest request,
            @Schema(description = "사업자 등록 서류 파일")
            MultipartFile businessRegistrationFile
    );

    @Hidden
    @Operation(summary = "사업자 정보 삭제", description = "사업자 정보를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> closeBusiness(
            @Schema(description = "사업자 ID", example = "1")
            Long ownerId
    );

    @Hidden
    @Operation(summary = "사업자 정보 조회", description = "사업자 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetOwnerResponse>> getMyBusiness(
            @Schema(description = "사업자 ID", example = "1")
            Long ownerId
    );

    @Operation(summary = "가게 정보 작성", description = "사업자가 신규 가게 정보를 생성합니다.")
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
                                        "  \"introduction\": \"따뜻한 마음으로 환영합니다!\",\n" +
                                        "  \"intro\": \"따뜻한 한 끼 식사\",\n" +
                                        "  \"tagNames\": [\"한식\", \"점심식사\", \"따뜻한\"],\n" +
                                        "  \"businessHours\": [\n" +
                                        "    {\n" +
                                        "      \"businessDay\": \"MON | TUE | WED | THU | FRI | SAT | SUN\",\n" +
                                        "      \"openTime\": \"09:00\",\n" +
                                        "      \"closeTime\": \"22:00\"\n" +
                                        "    }\n" +
                                        "  ],\n" +
                                        "  \"images\": [\n" +
                                        "    {\n" +
                                        "      \"fileKey\": \"image/uuid-photo.jpg\",\n" +
                                        "      \"filePath\": \"https://minio.example.com/bucket/image/uuid-photo.jpg\",\n" +
                                        "      \"displayOrder\": 0\n" +
                                        "    }\n" +
                                        "  ]\n" +
                                        "}"
                        )
                )
            )
            OpenStoreRequest request
    );

    @Operation(summary = "가게 정보 수정", description = "사업자가 자신의 가게 정보를 수정합니다.")
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
                                        "  \"address\": \"서울시 강남구 테헤란로 123\",\n" +
                                        "  \"phone\": \"0212345678\",\n" +
                                        "  \"lat\": \"37.5665\",\n" +
                                        "  \"lng\": \"126.9780\",\n" +
                                        "  \"introduction\": \"따뜻한 한 끼 식사\",\n" +
                                        "  \"intro\": \"따뜻한 마음으로 환영합니다!\",\n" +
                                        "  \"isOpen\": true,\n" +
                                        "  \"isSharing\": true,\n" +
                                        "  \"tagNames\": [\"한식\", \"점심식사\", \"따뜻한\"],\n" +
                                        "  \"businessHours\": [\n" +
                                        "    {\n" +
                                        "      \"businessDay\": \"MON | TUE | WED | THU | FRI | SAT | SUN\",\n" +
                                        "      \"openTime\": \"09:00\",\n" +
                                        "      \"closeTime\": \"22:00\"\n" +
                                        "    },\n" +
                                        "    {\n" +
                                        "      \"businessDay\": \"MON | TUE | WED | THU | FRI | SAT | SUN\",\n" +
                                        "      \"openTime\": \"10:00\",\n" +
                                        "      \"closeTime\": \"21:00\"\n" +
                                        "    }\n" +
                                        "  ],\n" +
                                        "  \"images\": [\n" +
                                        "    {\n" +
                                        "      \"fileKey\": \"image/uuid-photo.jpg\",\n" +
                                        "      \"filePath\": \"https://minio.example.com/bucket/image/uuid-photo.jpg\",\n" +
                                        "      \"displayOrder\": 0\n" +
                                        "    }\n" +
                                        "  ]\n" +
                                        "}"
                        )
                )
            )
            UpdateStoreRequest request
    );

    @Operation(summary = "가게 정보 삭제", description = "사업자가 자신의 가게 정보를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> closeStore(
            @Schema(description = "가게 ID", example = "1")
            Long storeId
    );

    @Operation(
        summary = "사업자 가게 목록 조회", 
        description = "사업자의 가게 목록을 조회합니다.",
        parameters = {
            @Parameter(name = "pageNum", description = "페이지 번호 (1부터 시작)", example = "1", schema = @Schema(type = "integer"), required = true),
            @Parameter(name = "perPage", description = "페이지당 항목 수", example = "10", schema = @Schema(type = "integer"), required = true),
            @Parameter(name = "sortField", description = "정렬 기준", example = "id", schema = @Schema(type = "string", allowableValues = {"id", "name", "favoriteCount"})),
            @Parameter(name = "sortDirection", description = "정렬 방향", example = "desc", schema = @Schema(type = "string", allowableValues = {"asc", "desc"}))
        }
    )
    ResponseEntity<ResponseDTO<GetMyStoresResponse>> getMyStores(
            @Schema(description = "사업자 가게 목록 조회 요청")
            @ParameterObject GetMyStoresRequest request
    );

    @Operation(
        summary = "예약 관리 목록 조회",
        description = "사업자의 예약 목록을 조회합니다. 파라미터 없으면 store_schedule별 최신 1건, pageNum/perPage/statusFilter 제공 시 페이징+필터 적용.",
        parameters = {
            @Parameter(name = "pageNum", description = "페이지 번호 (1부터)", example = "1", schema = @Schema(type = "integer")),
            @Parameter(name = "perPage", description = "페이지당 항목 수", example = "10", schema = @Schema(type = "integer")),
            @Parameter(name = "statusFilter", description = "예약 상태 필터", example = "ALL", schema = @Schema(type = "string", allowableValues = {"ALL", "WAITING", "CONFIRMED", "CANCELED", "COMPLETED"})),
            @Parameter(name = "sortField", description = "정렬 기준", example = "id", schema = @Schema(type = "string", allowableValues = {"id"})),
            @Parameter(name = "sortDirection", description = "정렬 방향", example = "desc", schema = @Schema(type = "string", allowableValues = {"asc", "desc"}))
        }
    )
    ResponseEntity<ResponseDTO<GetStoreBookingListResponse>> getStoreBookings(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @Schema(description = "예약 관리 목록 조회 요청")
            @ParameterObject GetStoreBookingsRequest request
    );

    @Operation(summary = "예약 관리 상세 조회", description = "사업자의 특정 예약의 상세 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetStoreBookingDetailResponse>> getStoreBooking(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );

    @Operation(summary = "예약 확정(승인)", description = "사업자가 예약을 확정합니다. (WAITING → CONFIRMED)")
    ResponseEntity<ResponseDTO<Void>> approveBook(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );

    @Operation(summary = "예약 완료", description = "사업자가 예약을 완료 처리합니다. (CONFIRMED → COMPLETED)")
    ResponseEntity<ResponseDTO<Void>> completeBook(
            @Schema(description = "예약 ID", example = "1")
            Long reservationId
    );

    @Operation(summary = "예약 취소", description = "사업자가 예약을 취소합니다.")
    ResponseEntity<ResponseDTO<Void>> cancelBook(
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

    @Operation(summary = "가게 스케줄 생성", description = "사업자가 가게의 예약 가능한 스케줄을 생성합니다.")
    ResponseEntity<ResponseDTO<Void>> setAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @RequestBody(
                    description = "스케줄 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SetAvailableDatesRequest.class),
                            examples = @ExampleObject(
                                    name = "스케줄 생성 예시",
                                    value = "{\n" +
                                            "  \"storeSchedules\": [\n" +
                                            "    {\n" +
                                            "      \"scheduleDate\": \"2024-12-31\",\n" +
                                            "      \"startTime\": \"14:00\",\n" +
                                            "      \"endTime\": \"15:00\",\n" +
                                            "      \"maxPeople\": 10\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"scheduleDate\": \"2025-01-01\",\n" +
                                            "      \"startTime\": \"16:00\",\n" +
                                            "      \"endTime\": \"17:00\",\n" +
                                            "      \"maxPeople\": 5\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            )
            SetAvailableDatesRequest request
    );

    @Operation(summary = "가게 스케줄 수정", description = "사업자가 가게의 예약 가능한 스케줄을 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @RequestBody(
                    description = "스케줄 수정 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateAvailableDatesRequest.class),
                            examples = @ExampleObject(
                                    name = "스케줄 수정 예시",
                                    value = "{\n" +
                                            "  \"storeSchedules\": [\n" +
                                            "    {\n" +
                                            "      \"id\": 1,\n" +
                                            "      \"scheduleDate\": \"2026-02-20\",\n" +
                                            "      \"startTime\": \"14:00\",\n" +
                                            "      \"endTime\": \"15:00\",\n" +
                                            "      \"maxPeople\": 10\n" +
                                            "    },\n" +
                                            "    {\n" +
                                            "      \"id\": 2,\n" +
                                            "      \"scheduleDate\": \"2025-01-01\",\n" +
                                            "      \"startTime\": \"16:00\",\n" +
                                            "      \"endTime\": \"17:00\",\n" +
                                            "      \"maxPeople\": 5\n" +
                                            "    }\n" +
                                            "  ]\n" +
                                            "}"
                            )
                    )
            )
            UpdateAvailableDatesRequest request
    );
    
    @Operation(summary = "가게 스케줄 삭제", description = "사업자가 가게의 예약 가능한 스케줄을 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> removeAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @RequestBody(
                    description = "스케줄 삭제 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RemoveAvailableDatesRequest.class),
                            examples = @ExampleObject(
                                    name = "스케줄 삭제 예시",
                                    value = "{\n" +
                                            "  \"storeScheduleIds\": [1, 2, 3]\n" +
                                            "}"
                            )
                    )
            )
            RemoveAvailableDatesRequest request
    );

    @Operation(summary = "요약된 예약 목록 조회", description = "요약된 예약 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<GetStoreBookingSummaryResponse>> getStoreBookingsSummary();
}