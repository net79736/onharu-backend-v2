package com.backend.onharu.domain.storeschedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

public class StoreScheduleRepositroyParam {
    /**
     * 가게 일정 ID로 단건 조회용 파라미터
     */
    public record GetStoreScheduleByIdParam(
            Long storeScheduleId
    ) {
    }

    /**
     * 가게 ID로 가게 일정 목록 조회용 파라미터
     */
    public record FindAllByStoreIdParam(
            Long storeId
    ) {
    }

    /**
     * 영업일로 가게 일정 목록 조회용 파라미터
     */
    public record FindAllByScheduleDateParam(
            LocalDate scheduleDate
    ) {
    }

    /**
     * 가게 ID와 영업일로 가게 일정 조회용 파라미터
     */
    public record FindByStoreIdAndScheduleDateParam(
            Long storeId,
            LocalDate scheduleDate
    ) {
    }

    /**
     * 특정 날짜에 해당하는 요일의 가게 일정 조회용 파라미터
     * 가게 ID는 옵셔널 (null이면 모든 가게)
     */
    public record FindByStoreIdAndDateParam(
            Long storeId,
            LocalDate date
    ) {
    }

    /**
     * 특정 시간 범위와 겹치는 가게 일정 조회용 파라미터
     * 요청한 시간 범위(startTime ~ endTime)와 일정의 영업시간(openTime ~ closeTime)이 겹치는 경우 조회
     * 가게 ID는 옵셔널 (null이면 모든 가게)
     */
    public record FindByStoreIdAndTimeRangeOverlapParam(
            Long storeId,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }
}
