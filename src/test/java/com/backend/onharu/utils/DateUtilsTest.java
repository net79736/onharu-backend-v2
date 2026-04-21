package com.backend.onharu.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.backend.onharu.domain.common.enums.WeekType;

@DisplayName("DateUtils 단위 테스트")
class DateUtilsTest {

    @Test
    @DisplayName("toLocalDate — WeekType 을 대표 LocalDate(2000-01-03 기준 월요일) 로 변환")
    void toLocalDate_mapsAllWeekTypes() {
        assertThat(DateUtils.toLocalDate(WeekType.MON)).isEqualTo(LocalDate.of(2000, 1, 3));
        assertThat(DateUtils.toLocalDate(WeekType.TUE)).isEqualTo(LocalDate.of(2000, 1, 4));
        assertThat(DateUtils.toLocalDate(WeekType.WED)).isEqualTo(LocalDate.of(2000, 1, 5));
        assertThat(DateUtils.toLocalDate(WeekType.THU)).isEqualTo(LocalDate.of(2000, 1, 6));
        assertThat(DateUtils.toLocalDate(WeekType.FRI)).isEqualTo(LocalDate.of(2000, 1, 7));
        assertThat(DateUtils.toLocalDate(WeekType.SAT)).isEqualTo(LocalDate.of(2000, 1, 8));
        assertThat(DateUtils.toLocalDate(WeekType.SUN)).isEqualTo(LocalDate.of(2000, 1, 9));
    }
}
