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
     * 영업일로 가게 일정 목록 조회
     */
    public record FindAllByBusinessDayQuery(
            LocalDate scheduleDate
    ) {
    }

    /**
     * 가게 ID와 영업일로 가게 일정 조회
     */
    public record FindByStoreIdAndBusinessDayQuery(
            Long storeId,
            LocalDate scheduleDate
    ) {
    }

    /**
     * 특정 날짜에 해당하는 요일의 가게 일정 조회
     * 가게 ID는 옵셔널 (null이면 모든 가게)
     */
    public record FindByStoreIdAndDateQuery(
            Long storeId,
            LocalDate scheduleDate
    ) {
    }

}
