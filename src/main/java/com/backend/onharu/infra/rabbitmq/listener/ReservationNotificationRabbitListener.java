package com.backend.onharu.infra.rabbitmq.listener;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.event.listener.ReservationNotificationHistoryListener;
import com.backend.onharu.event.model.ReservationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예약 알림 큐 구독 — 처리 완료 후에만 ACK 합니다.
 *
 * <p>payload 예시:</p>
 * <pre>
 * {
 *   "type": "RESERVATION_NOTIFICATION",
 *   "reservationId": 1,
 *   "ownerId": 10,
 *   "childId": 100,
 *   "notificationType": "RESERVATION_CREATED",
 *   "correlationId": "reservation-1-RESERVATION_CREATED"
 * }
 * </pre>
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "onharu.rabbitmq.enabled", havingValue = "true")
public class ReservationNotificationRabbitListener {

    private final ObjectMapper objectMapper;
    private final ReservationNotificationHistoryListener reservationNotificationHistoryHandler;

    @RabbitListener(queues = "${onharu.rabbitmq.reservation-notifications-queue:onharu.reservation.notifications}")
    public void onReservationNotification(
            String payload,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        ReservationEvent event;
        String correlationId = null;

        try {
            Parsed parsed = parse(payload);
            event = parsed.event;
            correlationId = parsed.correlationId;

            log.info("rabbit reservation notification received correlationId={} reservationId={} notificationType={}", correlationId, event.reservationId(), event.type());

            reservationNotificationHistoryHandler.handleReservationEvent(event);
        } catch (JsonProcessingException e) {
            log.warn("RabbitMQ 예약 알림 JSON 파싱 불가 — 재큐잉하지 않음: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        } catch (CoreException e) {
            log.warn("RabbitMQ 예약 알림 처리 실패(CoreException) — DLQ로 이동: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        } catch (Exception e) {
            log.error("RabbitMQ 예약 알림 처리 실패 — 재큐잉: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
            return;
        }

        channel.basicAck(deliveryTag, false);
        log.info("rabbit reservation notification acked correlationId={}", correlationId);
    }

    private Parsed parse(String payload) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(payload);

        Long reservationId = readLong(root, "reservationId");
        Long ownerId = readLong(root, "ownerId");
        Long childId = readLong(root, "childId");
        String notificationTypeRaw = readText(root, "notificationType");
        String correlationId = readText(root, "correlationId");

        if (reservationId == null || ownerId == null || childId == null || notificationTypeRaw == null) {
            throw new JsonProcessingException("required fields are missing") {
            };
        }

        NotificationHistoryType type = NotificationHistoryType.valueOf(notificationTypeRaw);
        return new Parsed(new ReservationEvent(reservationId, ownerId, childId, type), correlationId);
    }

    private static Long readLong(JsonNode root, String field) {
        JsonNode n = root.get(field);
        if (n == null || n.isNull()) return null;
        if (n.canConvertToLong()) return n.asLong();
        try {
            return Long.valueOf(n.asText());
        } catch (Exception e) {
            return null;
        }
    }

    private static String readText(JsonNode root, String field) {
        JsonNode n = root.get(field);
        if (n == null || n.isNull()) return null;
        String v = n.asText();
        return v == null || v.isBlank() ? null : v;
    }

    private record Parsed(ReservationEvent event, String correlationId) {
    }
}

