package com.backend.onharu.application.dto;

import java.util.List;

import com.backend.onharu.domain.reservation.model.Reservation;

/**
 * 요약된 예약 목록 조회 결과
 * 
 * UPCOMING_LIMIT : 다가오는 방문 예정 예약 최대 건수
 */
public record StoreBookingSummaryResult(
    List<Reservation> upcomingReservations
) {
    public static final int UPCOMING_LIMIT = 2;
}