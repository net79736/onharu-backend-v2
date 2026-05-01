package com.backend.onharu.utils;

/**
 * 숫자·계산 관련 유틸리티 클래스.
 * 소수점 버림, 반올림 등 공통 계산 로직을 통합 관리합니다.
 */
public final class NumberUtils {

    private NumberUtils() {
    }

    /**
     * 소수점 이하를 버리고 정수부만 반환합니다.
     *
     * @param value 원본 값 (예: 3.7)
     * @return 정수부만 남긴 double (예: 3.0)
     */
    public static double truncateToIntegerAsDouble(Double value) {
        if (value == null || Double.isNaN(value)) {
            return 0.0;
        }
        return value.longValue();
    }

    /**
     * 카운트 값을 Long으로 변환
     * 
     * @param value
     * @return
     */
    public static long toLong(Object value) {
        return toLong(value, 0L);
    }

    /**
     * 카운트 값을 Long으로 변환(변환 실패 시 fallback 반환)
     *
     * @param value    원본 값 (null 가능)
     * @param fallback 변환 실패 시 반환할 기본값
     * @return 변환 성공 시 long, 실패/null이면 fallback
     */
    public static long toLong(Object value, long fallback) {
        Long parsed = tryParseLong(value);
        return parsed != null ? parsed : fallback;
    }

    /**
     * Long 변환을 시도하고 실패 시 null 반환
     *
     * <p>호출 측에서 실패 케이스를 로깅/모니터링하고 싶을 때 사용합니다.</p>
     */
    public static Long tryParseLong(Object value) {
        if (value == null) return null;
        try {
            return Long.parseLong(String.valueOf(value));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
