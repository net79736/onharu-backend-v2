package com.backend.onharu.domain.storeschedule.dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

public class StoreScheduleRepositroyParam {
    /**
     * 가게 일정 ID로 단건 조회용 파라미터
     */
    public record GetStoreScheduleByIdParam(
            Long storeScheduleId
    ) {
        public GetStoreScheduleByIdParam {
            if (storeScheduleId == null) {
                throw new CoreException(ErrorType.StoreSchedule.STORE_SCHEDULE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 ID로 가게 일정 목록 조회용 파라미터
     */
    public record FindAllByStoreIdParam(
            Long storeId
    ) {
        public FindAllByStoreIdParam {
            if (storeId == null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 ID와 날짜로 가게 일정 목록 조회용 파라미터
     */
    public record FindAllByStoreIdAndScheduleDateParam(
            Long storeId,
            LocalDate scheduleDate
    ) {
        public FindAllByStoreIdAndScheduleDateParam {
            if (storeId == null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
            if (scheduleDate == null) {
                throw new CoreException(ErrorType.StoreSchedule.STORE_SCHEDULE_SCHEDULE_DATE_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 가게 ID와 연/월로 가게 일정 목록 조회용 파라미터
     */
    public record FindAllByStoreIdAndYearMonthParam(
            Long storeId,
            int year,
            int month
    ) {
        public FindAllByStoreIdAndYearMonthParam {
            if (storeId == null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
        }
    }

    /**
     * 특정 시간 범위와 겹치는 가게 일정 조회용 파라미터
     * 요청한 시간 범위(startTime ~ endTime)와 일정의 영업시간(openTime ~ closeTime)이 겹치는 경우 조회
     */
    public record FindByStoreIdAndTimeRangeOverlapParam(
            Long storeId,
            LocalTime startTime,
            LocalTime endTime
    ) {
        public FindByStoreIdAndTimeRangeOverlapParam {
            if (storeId != null) {
                throw new CoreException(ErrorType.Store.STORE_ID_MUST_NOT_BE_NULL);
            }
            if (startTime == null) {
                throw new CoreException(ErrorType.StoreSchedule.STORE_SCHEDULE_START_TIME_MUST_NOT_BE_NULL);
            }
            if (endTime == null) {
                throw new CoreException(ErrorType.StoreSchedule.STORE_SCHEDULE_END_TIME_MUST_NOT_BE_NULL);
            }
        }
    }
}
