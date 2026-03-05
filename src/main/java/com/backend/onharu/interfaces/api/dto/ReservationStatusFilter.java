package com.backend.onharu.interfaces.api.dto;

import java.util.Optional;

import com.backend.onharu.domain.common.enums.ReservationType;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 예약 목록 조회 시 상태 필터 (API 전용)
 * <p>
 * - ALL: 전체 조회 (상태 필터 미적용)
 * - 그 외: 해당 예약 상태로 필터링
 * </p>
 */
@Schema(description = "예약 상태 필터", allowableValues = {"ALL", "WAITING", "CONFIRMED", "CANCELED", "COMPLETED"})
public enum ReservationStatusFilter {
    ALL,
    WAITING,
    CONFIRMED,
    CANCELED,
    COMPLETED;

    /**
     * 도메인 ReservationType으로 변환.
     * ALL인 경우 Optional.empty() 반환 (필터 미적용).
     */
    public Optional<ReservationType> toReservationType() {
        return this == ALL
                ? Optional.empty()
                : Optional.of(ReservationType.valueOf(this.name()));
    }
}
