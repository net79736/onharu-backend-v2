package com.backend.onharu.infra.rabbitmq;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.backend.onharu.application.ChildFacade;
import com.backend.onharu.domain.reservation.dto.ReservationCommand.CreateReservationCommand;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 예약 요청 큐 구독 — HTTP 없이 RabbitMQ → DB 예약 생성 경로를 제공합니다.
 *
 * 메시지(JSON) 예시:
 * {
 *   "childId": 1,
 *   "storeScheduleId": 10,
 *   "people": 1,
 *   "correlationId": "batch-202605-0001" // optional
 * }
 *
 * 주의:
 * - 컨슈머는 세션(SecurityContext)이 없으므로 childId는 반드시 메시지에 포함되어야 합니다.
 * - 중복/검증 실패 등 도메인 예외(CoreException)는 재큐잉하지 않고 DLQ로 보냅니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "onharu.rabbitmq.enabled", havingValue = "true")
public class ReservationRequestsRabbitListener {

    private final ObjectMapper objectMapper;
    private final ChildFacade childFacade;

    @RabbitListener(queues = "${onharu.rabbitmq.reservation-requests-queue:onharu.reservation.requests}")
    public void onReservationRequest(
            String payload,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        try {
            ReservationRequest req = parse(payload);
            log.info(
                    "rabbit reservation request received correlationId={} childId={} storeScheduleId={} people={}",
                    req.correlationId, req.childId, req.storeScheduleId, req.people
            );

            childFacade.reserve(new CreateReservationCommand(req.childId, req.storeScheduleId, req.people));
        } catch (JsonProcessingException e) {
            // 페이로드가 깨졌으면 재시도해도 성공할 확률이 낮으므로 DLQ로.
            log.warn("RabbitMQ 예약 요청 JSON 파싱 불가 — 재큐잉하지 않음: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        } catch (CoreException e) {
            // 도메인 검증 실패/중복 등: 재시도해도 계속 실패할 가능성이 높으므로 DLQ로.
            log.warn("RabbitMQ 예약 요청 처리 실패(CoreException) — DLQ로 이동: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        } catch (Exception e) {
            // 일시 장애(락/DB 순간 오류 등) 가능성을 고려해 재큐잉.
            log.error("RabbitMQ 예약 요청 처리 실패 — 재큐잉: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
            return;
        }

        channel.basicAck(deliveryTag, false);
    }

    private ReservationRequest parse(String payload) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(payload);

        Long childId = readLong(root, "childId");
        Long storeScheduleId = readLong(root, "storeScheduleId");
        Integer people = readInt(root, "people");
        String correlationId = readText(root, "correlationId");

        if (childId == null) throw new CoreException(ErrorType.Child.CHILD_ID_MUST_NOT_BE_NULL);
        if (storeScheduleId == null) throw new CoreException(ErrorType.StoreSchedule.STORE_SCHEDULE_ID_MUST_NOT_BE_NULL);
        if (people == null) throw new CoreException(ErrorType.Reservation.RESERVATION_PEOPLE_MUST_NOT_BE_NULL);

        return new ReservationRequest(childId, storeScheduleId, people, correlationId);
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

    private static Integer readInt(JsonNode root, String field) {
        JsonNode n = root.get(field);
        if (n == null || n.isNull()) return null;
        if (n.canConvertToInt()) return n.asInt();
        try {
            return Integer.valueOf(n.asText());
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

    private record ReservationRequest(
            Long childId,
            Long storeScheduleId,
            Integer people,
            String correlationId
    ) {
    }
}
