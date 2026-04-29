package com.backend.onharu.interfaces.shceduler;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.reservation.service.ReservationCommandService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Deprecated
@Slf4j
// @Component
@RequiredArgsConstructor
public class ReservationScheduler {

    private final ReservationCommandService reservationCommandService;

    @Scheduled(cron = "0 0 0 * * *") // 매일 00:00:00에 실행
    @Transactional
    public void expireReservations() {
        log.info("만료 예약 처리 시작 스케줄러 실행");
        reservationCommandService.expireOverDueReservations();
    }
}
