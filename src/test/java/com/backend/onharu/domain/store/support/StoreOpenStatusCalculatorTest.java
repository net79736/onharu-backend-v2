package com.backend.onharu.domain.store.support;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import com.backend.onharu.domain.common.enums.WeekType;
import com.backend.onharu.domain.store.dto.StoreCacheDto;
import com.backend.onharu.domain.store.model.BusinessHours;

@DisplayName("StoreOpenStatusCalculator 단위 테스트")
class StoreOpenStatusCalculatorTest {

    // 2026-04-20 (월요일) 14:00
    private static final LocalDateTime MON_14_00 = LocalDateTime.of(2026, 4, 20, 14, 0);
    // 2026-04-21 (화요일) 09:30
    private static final LocalDateTime TUE_09_30 = LocalDateTime.of(2026, 4, 21, 9, 30);
    // 2026-04-19 (일요일) 23:00
    private static final LocalDateTime SUN_23_00 = LocalDateTime.of(2026, 4, 19, 23, 0);

    private BusinessHours hours(WeekType day, String open, String close) {
        return BusinessHours.builder()
                .businessDay(day)
                .openTime(LocalTime.parse(open))
                .closeTime(LocalTime.parse(close))
                .build();
    }

    @Nested
    @DisplayName("isOpenNow (엔티티용)")
    class IsOpenNowEntity {

        @Test
        @DisplayName("manualOpenFlag 가 false 면 즉시 false 반환")
        void manualOpenFalse_returnsFalse() {
            boolean result = StoreOpenStatusCalculator.isOpenNow(
                    false,
                    List.of(hours(WeekType.MON, "09:00", "22:00")),
                    MON_14_00);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("manualOpenFlag 가 null 이면 false")
        void manualOpenNull_returnsFalse() {
            assertThat(StoreOpenStatusCalculator.isOpenNow(
                    null, List.of(hours(WeekType.MON, "09:00", "22:00")), MON_14_00)).isFalse();
        }

        @Test
        @DisplayName("businessHours 가 비어있으면 false")
        void emptyHours_returnsFalse() {
            assertThat(StoreOpenStatusCalculator.isOpenNow(true, List.of(), MON_14_00)).isFalse();
            assertThat(StoreOpenStatusCalculator.isOpenNow(true, null, MON_14_00)).isFalse();
        }

        @Test
        @DisplayName("now 가 null 이면 false")
        void nullNow_returnsFalse() {
            assertThat(StoreOpenStatusCalculator.isOpenNow(
                    true, List.of(hours(WeekType.MON, "09:00", "22:00")), null)).isFalse();
        }

        @Test
        @DisplayName("오늘 영업시간 내에 있으면 true")
        void withinHours_returnsTrue() {
            boolean result = StoreOpenStatusCalculator.isOpenNow(
                    true,
                    List.of(hours(WeekType.MON, "09:00", "22:00")),
                    MON_14_00);
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("오늘 요일에 영업시간이 없으면 false")
        void noMatchingDay_returnsFalse() {
            // 월요일인데 화요일 영업시간만 있음
            boolean result = StoreOpenStatusCalculator.isOpenNow(
                    true,
                    List.of(hours(WeekType.TUE, "09:00", "22:00")),
                    MON_14_00);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("openTime == closeTime (invalid) 이면 false")
        void sameOpenAndClose_returnsFalse() {
            boolean result = StoreOpenStatusCalculator.isOpenNow(
                    true,
                    List.of(hours(WeekType.MON, "14:00", "14:00")),
                    MON_14_00);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("자정 넘김(openTime > closeTime) 은 false 로 판정")
        void crossMidnight_returnsFalse() {
            boolean result = StoreOpenStatusCalculator.isOpenNow(
                    true,
                    List.of(hours(WeekType.MON, "22:00", "02:00")),
                    MON_14_00);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("openTime 이전 시각이면 false")
        void beforeOpen_returnsFalse() {
            boolean result = StoreOpenStatusCalculator.isOpenNow(
                    true,
                    List.of(hours(WeekType.TUE, "10:00", "18:00")),
                    TUE_09_30);
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("closeTime 이후 시각이면 false")
        void afterClose_returnsFalse() {
            boolean result = StoreOpenStatusCalculator.isOpenNow(
                    true,
                    List.of(hours(WeekType.SUN, "09:00", "22:00")),
                    SUN_23_00);
            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("isOpenNowFromCache (캐시 DTO용)")
    class IsOpenNowFromCache {

        private StoreCacheDto.BusinessHoursDto cacheHours(String day, String open, String close) {
            return new StoreCacheDto.BusinessHoursDto(null, day, open, close);
        }

        @Test
        @DisplayName("manualOpenFlag 가 false 면 false")
        void manualOpenFalse_returnsFalse() {
            assertThat(StoreOpenStatusCalculator.isOpenNowFromCache(
                    false, List.of(cacheHours("MON", "09:00", "22:00")), MON_14_00)).isFalse();
        }

        @Test
        @DisplayName("영업시간 내에 있으면 true")
        void withinHours_returnsTrue() {
            assertThat(StoreOpenStatusCalculator.isOpenNowFromCache(
                    true, List.of(cacheHours("MON", "09:00", "22:00")), MON_14_00)).isTrue();
        }

        @Test
        @DisplayName("businessDay 파싱 실패(잘못된 값) 면 해당 항목은 스킵되어 false")
        void invalidBusinessDay_returnsFalse() {
            assertThat(StoreOpenStatusCalculator.isOpenNowFromCache(
                    true, List.of(cacheHours("INVALID_DAY", "09:00", "22:00")), MON_14_00)).isFalse();
        }

        @Test
        @DisplayName("time 파싱 실패면 해당 항목 스킵되어 false")
        void invalidTime_returnsFalse() {
            assertThat(StoreOpenStatusCalculator.isOpenNowFromCache(
                    true, List.of(cacheHours("MON", "not-a-time", "22:00")), MON_14_00)).isFalse();
        }

        @Test
        @DisplayName("businessHours 가 null 이면 false")
        void nullHours_returnsFalse() {
            assertThat(StoreOpenStatusCalculator.isOpenNowFromCache(true, null, MON_14_00)).isFalse();
        }
    }

    @Nested
    @DisplayName("isOpenNow(Store, now) 오버로드")
    class IsOpenNowStoreOverload {

        @Test
        @DisplayName("store 가 null 이면 false")
        void nullStore_returnsFalse() {
            assertThat(StoreOpenStatusCalculator.isOpenNow(null, MON_14_00)).isFalse();
        }
    }
}
