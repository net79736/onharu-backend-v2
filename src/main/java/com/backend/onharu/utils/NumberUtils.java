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
    public static double truncateToIntegerAsDouble(double value) {
        return (double) (long) value;
    }
}
