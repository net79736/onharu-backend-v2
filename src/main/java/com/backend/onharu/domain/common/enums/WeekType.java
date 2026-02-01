package com.backend.onharu.domain.common.enums;

import java.time.DayOfWeek;
import java.time.LocalDate;

/**
 * 요일을 정의하는 enum입니다.
 * 
 * 요일:
 * - MON: 월요일
 * - TUE: 화요일
 * - WED: 수요일
 * - THU: 목요일
 * - FRI: 금요일
 * - SAT: 토요일
 * - SUN: 일요일
 */
public enum WeekType {
    MON,
    TUE,
    WED,
    THU,
    FRI,
    SAT,
    SUN;

    /**
     * LocalDate를 WeekType으로 변환합니다.
     * 
     * @param date 변환할 날짜
     * @return 변환된 WeekType
     */
    public static WeekType fromLocalDate(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return switch (dayOfWeek) {
            case MONDAY -> MON;
            case TUESDAY -> TUE;
            case WEDNESDAY -> WED;
            case THURSDAY -> THU;
            case FRIDAY -> FRI;
            case SATURDAY -> SAT;
            case SUNDAY -> SUN;
        };
    }

    /**
     * DayOfWeek를 WeekType으로 변환합니다.
     * 
     * @param dayOfWeek 변환할 요일
     * @return 변환된 WeekType
     */
    public static WeekType fromDayOfWeek(DayOfWeek dayOfWeek) {
        return switch (dayOfWeek) {
            case MONDAY -> MON;
            case TUESDAY -> TUE;
            case WEDNESDAY -> WED;
            case THURSDAY -> THU;
            case FRIDAY -> FRI;
            case SATURDAY -> SAT;
            case SUNDAY -> SUN;
        };
    }
}
