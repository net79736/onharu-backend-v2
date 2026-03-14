package com.backend.onharu.application.dto;

import java.util.List;

import com.backend.onharu.domain.reservation.model.Reservation;

/**
 * 마이페이지 요약 예약 조회 결과
 *
 * UPCOMING_LIMIT      : 다가오는 방문 예정 예약 최대 건수
 * REVIEW_TARGET_LIMIT : 리뷰 작성 대상 예약 최대 건수
 */
public record MyBookingSummaryResult(
    List<Reservation> upcomingReservations,
    List<Reservation> reviewTargetReservations
) {
    public static final int UPCOMING_LIMIT = 2;
    public static final int REVIEW_TARGET_LIMIT = 2;
}
