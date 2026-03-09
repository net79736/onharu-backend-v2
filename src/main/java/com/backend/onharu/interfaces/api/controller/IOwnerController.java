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
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.GetStoreBookingsRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.RemoveAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.SetAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.OwnerControllerDto.UpdateOwnerRequest;

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
            @Parameter(name = "sortField", description = "정렬 기준", example = "id", schema = @Schema(type = "string")),
            @Parameter(name = "sortDirection", description = "정렬 방향", example = "desc", schema = @Schema(type = "string"))
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

    @Operation(summary = "예약 가능한 날짜 생성", description = "예약 가능한 날짜를 생성합니다.")
    ResponseEntity<ResponseDTO<Void>> setAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @RequestBody(
                    description = "예약 가능한 날짜 생성 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SetAvailableDatesRequest.class),
                            examples = @ExampleObject(
                                    name = "예약 가능한 날짜 생성 예시",
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

    @Operation(summary = "예약 가능한 날짜 수정", description = "예약 가능한 날짜를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @RequestBody(
                    description = "예약 가능한 날짜 수정 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateAvailableDatesRequest.class),
                            examples = @ExampleObject(
                                    name = "예약 가능한 날짜 수정 예시",
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
    
    @Operation(summary = "예약 가능한 날짜 삭제", description = "예약 가능한 날짜를 삭제합니다.")
    ResponseEntity<ResponseDTO<Void>> removeAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,
            @RequestBody(
                    description = "예약 가능한 날짜 삭제 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RemoveAvailableDatesRequest.class),
                            examples = @ExampleObject(
                                    name = "예약 가능한 날짜 삭제 예시",
                                    value = "{\n" +
                                            "  \"storeScheduleIds\": [1, 2, 3]\n" +
                                            "}"
                            )
                    )
            )
            RemoveAvailableDatesRequest request
    );
}