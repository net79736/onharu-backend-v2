package com.backend.onharu.domain.storeschedule.dto;

import java.time.LocalDate;

public class StoreScheduleQuery {
    /**
     * 가게 일정 ID로 단건 조회
     */
    public record GetStoreScheduleByIdQuery(
            Long storeScheduleId
    ) {
    }

    /**
     * 가게 ID로 가게 일정 목록 조회
     */
    public record FindAllByStoreIdQuery(
            Long storeId
    ) {
    }

    /**
     * 가게 ID와 날짜로 가게 일정 목록 조회
     */
    public record FindAllByStoreIdAndScheduleDateQuery(
            Long storeId,
            LocalDate scheduleDate
    ) {
    }

    /**
     * 영업일로 가게 일정 목록 조회
     */
    public record FindAllByBusinessDayQuery(
            LocalDate scheduleDate
    ) {
    }

    /**
     * 가게 ID와 연/월로 가게 일정 목록 조회
     */
    public record FindAllByStoreIdAndYearMonthQuery(
            Long storeId,
            int year,
            int month
    ) {
    }
}
