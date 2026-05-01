package com.backend.onharu.domain.event;

import com.backend.onharu.event.model.ReservationEvent;

/**
 * 예약 알림 이벤트를 RabbitMQ 로 발행하는 포트
 */
public interface ReservationNotificationRabbitPublishPort {

    void publishReservationNotification(ReservationEvent event);
}

