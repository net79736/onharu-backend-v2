package com.backend.onharu.interfaces.api.controller;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.GetAvailableDatesRequest;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.GetAvailableDatesResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Store Schedule", description = "가게 날짜 관리 API")
public interface IStoreScheduleController {
    
    @Operation(summary = "예약 가능한 날짜 조회", description = "예약 가능한 날짜를 조회합니다.")
    ResponseEntity<ResponseDTO<GetAvailableDatesResponse>> getAvailableDates(
            @Schema(description = "가게 ID", example = "1")
            Long storeId,  
            @Schema(description = "예약 가능한 날짜 요청", example = "2026-02-20")
            @ParameterObject GetAvailableDatesRequest request
    );
}
