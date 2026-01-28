package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import io.swagger.v3.oas.annotations.media.Schema;

public class StoreScheduleControllerDto {

    public record AvailableScheduleResponse(
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
            Integer maxPeople
    ) {
    }

    public record GetAvailableDatesResponse(
            @Schema(description = "예약 가능한 일정 목록")
            List<AvailableScheduleResponse> availableSchedules
    ) {
    }
}
