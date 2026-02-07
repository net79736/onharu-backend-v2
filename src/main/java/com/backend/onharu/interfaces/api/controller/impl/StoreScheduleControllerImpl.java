package com.backend.onharu.interfaces.api.controller.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.application.StoreScheduleFacade;
import com.backend.onharu.application.dto.StoreScheduleWithAvailability;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IStoreScheduleController;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.GetAvailableDatesResponse;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.StoreScheduleResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 가게 날짜 관리 API를 제공하는 컨트롤러 구현체입니다.
 * 
 * 가게의 예약 가능한 날짜를 조회합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/store-schedules")
@RequiredArgsConstructor
public class StoreScheduleControllerImpl implements IStoreScheduleController {

    private final StoreScheduleFacade storeScheduleFacade;


    /**
     * 예약 가능한 날짜 조회
     * 
     * GET /stores/{storeId}/available-dates
     * 가게의 예약 가능한 날짜를 조회합니다.
     *
     * @param storeId 가게 ID
     * @return 예약 가능한 날짜 목록
     */
    @Override
    @GetMapping("/{storeId}/available-dates")
    public ResponseEntity<ResponseDTO<GetAvailableDatesResponse>> getAvailableDates(
            @PathVariable("storeId") Long storeId
    ) {
        log.info("예약 가능한 날짜 조회 요청: storeId={}", storeId);

        // Facade에서 모든 일정과 예약 가능 여부를 함께 조회
        StoreScheduleWithAvailability result = storeScheduleFacade.getAllStoreSchedulesWithAvailability(storeId);

        // DTO로 변환
        List<StoreScheduleResponse> storeScheduleResponses = result.allSchedules().stream()
                .map(schedule -> new StoreScheduleResponse(
                        schedule,
                        result.availableScheduleIds().contains(schedule.getId())
                ))
                .collect(Collectors.toList());

        GetAvailableDatesResponse response = new GetAvailableDatesResponse(storeScheduleResponses);

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }
}
