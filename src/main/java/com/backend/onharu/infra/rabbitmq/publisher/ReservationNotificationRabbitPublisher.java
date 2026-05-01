package com.backend.onharu.infra.rabbitmq.publisher;

import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.backend.onharu.domain.event.ReservationNotificationRabbitPublishPort;
import com.backend.onharu.event.model.ReservationEvent;
import com.backend.onharu.event.model.ReservationMessageType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예약 알림 이벤트를 RabbitMQ로 발행합니다.
 *
 * <p>발행은 기본 exchange("") + routingKey=큐이름(=목적지) 방식으로 전송합니다.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "onharu.rabbitmq.enabled", havingValue = "true")
public class ReservationNotificationRabbitPublisher implements ReservationNotificationRabbitPublishPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${onharu.rabbitmq.reservation-notifications-queue:onharu.reservation.notifications}")
    private String reservationNotificationsQueue;

    @Override
    public void publishReservationNotification(ReservationEvent event) {
        String correlationId = buildCorrelationId(event);
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "type", ReservationMessageType.RESERVATION_NOTIFICATION.value(),
                            "reservationId", event.reservationId(),
                            "ownerId", event.ownerId(),
                            "childId", event.childId(),
                            "notificationType", event.type().name(),
                            "correlationId", correlationId
                    )
            );
            rabbitTemplate.convertAndSend("", reservationNotificationsQueue, payload);
            log.info(
                    "rabbit reservation notification published correlationId={} reservationId={} notificationType={}",
                    correlationId, event.reservationId(), event.type()
            );
        } catch (JsonProcessingException e) {
            log.warn("RabbitMQ 예약 알림 JSON 직렬화 실패: {}", e.getMessage());
            throw new IllegalStateException("RabbitMQ 예약 알림 JSON 직렬화 실패", e);
        }
    }

    private static String buildCorrelationId(ReservationEvent event) {
        return "reservation-" + event.reservationId() + "-" + event.type().name();
    }
}

