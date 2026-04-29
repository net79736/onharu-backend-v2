package com.backend.onharu.infra.rabbitmq;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.event.listener.ReservationNotificationHistoryHandler;
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
    private final ReservationNotificationHistoryHandler reservationNotificationHistoryHandler;

    /**
     * 이 메서드는 예약 알림 관련 메시지가 RabbitMQ 큐
     * ({@code ${onharu.rabbitmq.reservation-notifications-queue:onharu.reservation.notifications}})
     * 에 도착할 때마다 실행됩니다.
     * 즉, 새로운 예약 상태 알림 이벤트가 큐에 쌓이면 자동으로 호출되어
     * 메시지 파싱 및 예약 알림 처리 후 수동으로 ack/nack 처리합니다.
     * 
     * @param payload
     * @param channel
     * @param deliveryTag
     * @throws IOException
     */
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
            event = parsed.event; // 예약 이벤트 객체 (ex: ReservationEvent(1L, 10L, 100L, NotificationHistoryType.RESERVATION_CREATED))
            correlationId = parsed.correlationId; // 상관 관계 ID (ex: "reservation-1-RESERVATION_CREATED")

            log.info("rabbit reservation notification received correlationId={} reservationId={} notificationType={}", correlationId, event.reservationId(), event.type());

            reservationNotificationHistoryHandler.handleReservationEvent(event);
        } catch (JsonProcessingException e) {
            log.warn("RabbitMQ 예약 알림 JSON 파싱 불가 — 재큐잉하지 않음: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        } catch (CoreException e) {
            // 도메인 예외(검증/존재하지 않는 참조 등)는 재시도해도 계속 실패할 가능성이 높음 → DLQ.
            log.warn("RabbitMQ 예약 알림 처리 실패(CoreException) — DLQ로 이동: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        } catch (Exception e) {
            // 일시 장애(락/DB 순간 오류 등) 가능성을 고려해 재큐잉.
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

