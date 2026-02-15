package com.backend.onharu.utils;

import java.time.LocalDate;

import com.backend.onharu.domain.common.enums.WeekType;

public class DateUtils {

    /** WeekType을 대표 LocalDate로 변환 (API 응답용). */
    public static LocalDate toLocalDate(WeekType weekType) {
        return switch (weekType) {
            case MON -> LocalDate.of(2000, 1, 3);
            case TUE -> LocalDate.of(2000, 1, 4);
            case WED -> LocalDate.of(2000, 1, 5);
            case THU -> LocalDate.of(2000, 1, 6);
            case FRI -> LocalDate.of(2000, 1, 7);
            case SAT -> LocalDate.of(2000, 1, 8);
            case SUN -> LocalDate.of(2000, 1, 9);
        };
    }
    
}
