package com.backend.onharu.event.listener;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.onharu.domain.event.ReservationNotificationRabbitPublishPort;
import com.backend.onharu.event.model.ReservationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListner {
    private final ObjectProvider<ReservationNotificationRabbitPublishPort> reservationNotificationRabbitPublisherProvider; // RabbitMQ 발행 서비스 제공자
    private final ReservationNotificationHistoryHandler reservationNotificationHistoryHandler; // 알림 히스토리 저장 핸들러

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationEvent(ReservationEvent event) {
        ReservationNotificationRabbitPublishPort port = reservationNotificationRabbitPublisherProvider.getIfAvailable();
        if (port != null) {
            port.publishReservationNotification(event);
            return;
        }

        // RabbitMQ가 꺼진 환경에서도 알림 기능이 동작하도록 로컬 저장 경로를 fallback으로 둡니다.
        reservationNotificationHistoryHandler.handleReservationEvent(event);
    }
}
