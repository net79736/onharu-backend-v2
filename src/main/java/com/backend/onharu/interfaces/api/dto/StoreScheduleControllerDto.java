package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.backend.onharu.domain.storeschedule.model.StoreSchedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;

public class StoreScheduleControllerDto {

    public record StoreScheduleResponse(
            @Schema(description = "일정 ID", example = "1")
            Long id,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "일정 날짜", example = "2024-12-31")
            LocalDate scheduleDate,

            @Schema(description = "시작 시간", example = "14:00")
            LocalTime startTime,

            @Schema(description = "종료 시간", example = "15:00")
            LocalTime endTime,

            @Schema(description = "최대 인원", example = "10")
            Integer maxPeople,

            @Schema(description = "예약 가능 여부", example = "true")
            Boolean isAvailable
    ) {
        /**
         * StoreSchedule 도메인 모델을 StoreScheduleResponse로 변환합니다.
         * 
         * @param storeSchedule 변환할 StoreSchedule 도메인 모델
         * @param isAvailable 예약 가능 여부
         */
        public StoreScheduleResponse(
                StoreSchedule storeSchedule,
                Boolean isAvailable
        ) {
            this(
                    storeSchedule.getId(),
                    storeSchedule.getStore().getId(),
                    storeSchedule.getScheduleDate(),
                    storeSchedule.getStartTime(),
                    storeSchedule.getEndTime(),
                    storeSchedule.getMaxPeople(),
                    isAvailable
            );
        }
    }

    public record GetAvailableDatesRequest(
            @NotNull(message = "예약 가능한 날짜는 필수입니다.")
            @FutureOrPresent(message = "예약 가능한 날짜는 오늘 이후여야 합니다.")
            @Schema(description = "예약 가능한 날짜", example = "2024-12-31")
            LocalDate availableDate
    ) {
    }

    public record GetAvailableDatesResponse(
            @Schema(description = "예약 가능한 일정 목록")
            List<StoreScheduleResponse> storeSchedules
    ) {
    }
}
