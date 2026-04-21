package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.hibernate.validator.constraints.Range;

import com.backend.onharu.domain.storeschedule.model.StoreSchedule;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class StoreScheduleControllerDto {

    /**
     * 가게 스케줄 조회 요청 파라미터
     * - year, month 는 필수
     * - day 는 선택: 없으면 월별 날짜 요약, 있으면 해당 날짜의 시간대 상세 반환
     */
    public record GetStoreSchedulesRequest(
            @NotNull(message = "연도는 필수입니다.")
            @Positive(message = "연도는 양수여야 합니다.")
            @Schema(description = "연도", example = "2026")
            Integer year,

            @NotNull(message = "월은 필수입니다.")
            @Range(min = 1, max = 12, message = "월은 1에서 12 사이여야 합니다.")
            @Schema(description = "월", example = "3")
            Integer month,

            @Range(min = 1, max = 31, message = "일은 1에서 31 사이여야 합니다.")
            @Schema(description = "일 (선택, 없으면 월별 요약 반환)", example = "15", nullable = true)
            Integer day
    ) {
    }

    /**
     * 월별 조회 시 날짜 하나의 요약 정보 (day 없이 year+month 조회 시 사용)
     * 날짜별로 예약 가능한 슬롯 수와 해당 날짜의 시간대 슬롯 목록을 반환합니다.
     */
    public record DateSummary(
            @Schema(description = "날짜", example = "2026-03-15")
            LocalDate date,

            @Schema(description = "예약 가능한 슬롯 수", example = "3")
            int availableSlots,

            @Schema(description = "해당 날짜의 시간대 슬롯 목록")
            List<ScheduleSlot> scheduleSlots
    ) {
    }

    /**
     * 시간대 단위 스케줄 슬롯 (특정 날짜의 특정 시간대 예약 단위)
     */
    public record ScheduleSlot(
            @Schema(description = "일정 ID", example = "1")
            Long id,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "일정 날짜", example = "2026-03-15")
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
        public ScheduleSlot(StoreSchedule storeSchedule, Boolean isAvailable) {
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

    /**
     * 가게 스케줄 조회 응답
     * - day 없이 조회: dateSummaries 채워짐, scheduleSlots = null
     * - day 포함 조회: scheduleSlots 채워짐, dateSummaries = null
     */
    public record GetStoreSchedulesResponse(
            @Schema(description = "월별 날짜 요약 목록 (년/월 조회 시)")
            List<DateSummary> dateSummaries,

            @Schema(description = "시간대 슬롯 목록 (년/월/일 조회 시)")
            List<ScheduleSlot> scheduleSlots
    ) {
        public static GetStoreSchedulesResponse ofDateSummaries(List<DateSummary> summaries) {
            return new GetStoreSchedulesResponse(summaries, null);
        }

        public static GetStoreSchedulesResponse ofScheduleSlots(List<ScheduleSlot> slots) {
            return new GetStoreSchedulesResponse(null, slots);
        }
    }
}
