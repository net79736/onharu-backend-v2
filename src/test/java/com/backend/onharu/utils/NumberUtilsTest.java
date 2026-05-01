package com.backend.onharu.utils;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("NumberUtils 단위 테스트")
class NumberUtilsTest {

    @Nested
    @DisplayName("truncateToIntegerAsDouble")
    class TruncateToIntegerAsDouble {

        @Test
        @DisplayName("소수점 이하 버리고 정수부만 double 로 반환")
        void truncate_positive() {
            assertThat(NumberUtils.truncateToIntegerAsDouble(3.7)).isEqualTo(3.0);
            assertThat(NumberUtils.truncateToIntegerAsDouble(0.0)).isEqualTo(0.0);
            assertThat(NumberUtils.truncateToIntegerAsDouble(10.999)).isEqualTo(10.0);
        }

        @Test
        @DisplayName("음수는 0 방향(절삭)으로 처리")
        void truncate_negative() {
            assertThat(NumberUtils.truncateToIntegerAsDouble(-3.7)).isEqualTo(-3.0);
        }

        @Test
        @DisplayName("null 은 0.0 반환")
        void truncate_null_returnsZero() {
            assertThat(NumberUtils.truncateToIntegerAsDouble(null)).isEqualTo(0.0);
        }

        @Test
        @DisplayName("NaN 은 0.0 반환")
        void truncate_nan_returnsZero() {
            assertThat(NumberUtils.truncateToIntegerAsDouble(Double.NaN)).isEqualTo(0.0);
        }
    }

    @Nested
    @DisplayName("toLong")
    class ToLong {

        @Test
        @DisplayName("숫자 문자열은 Long 으로 변환")
        void toLong_numericString() {
            assertThat(NumberUtils.toLong("123")).isEqualTo(123L);
            assertThat(NumberUtils.toLong("-45")).isEqualTo(-45L);
        }

        @Test
        @DisplayName("정수형 객체도 변환 가능")
        void toLong_integerObject() {
            assertThat(NumberUtils.toLong(Integer.valueOf(42))).isEqualTo(42L);
            assertThat(NumberUtils.toLong(Long.valueOf(77L))).isEqualTo(77L);
        }

        @Test
        @DisplayName("null 은 0")
        void toLong_null_returnsZero() {
            assertThat(NumberUtils.toLong(null)).isZero();
        }

        @Test
        @DisplayName("파싱 실패 시 0")
        void toLong_invalidString_returnsZero() {
            assertThat(NumberUtils.toLong("not-a-number")).isZero();
            assertThat(NumberUtils.toLong("")).isZero();
        }
    }
}
