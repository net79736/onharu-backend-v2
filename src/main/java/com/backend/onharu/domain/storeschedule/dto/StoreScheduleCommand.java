package com.backend.onharu.domain.storeschedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class StoreScheduleCommand {
    /**
     * 가게 일정 생성 커맨드
     */
    public record CreateStoreScheduleCommand(
            Long storeId,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime,
            Integer maxPeople
    ) {
    }

    /**
     * 가게 일정 수정 커맨드
     */
    public record UpdateStoreScheduleCommand(
            Long id,
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime,
            Integer maxPeople
    ) {
    }

    /**
     * 가게 일정 삭제 커맨드
     */
    public record DeleteStoreScheduleCommand(
            Long id
    ) {
    }
}
