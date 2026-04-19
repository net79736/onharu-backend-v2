package com.backend.onharu.infra.rabbitmq;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 이벤트 큐 구독 — 수동 ACK 로 처리 완료 후에만 큐에서 제거합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "onharu.rabbitmq.enabled", havingValue = "true")
public class OnharuChatEventsRabbitListener {

    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "${onharu.rabbitmq.chat-events-queue:onharu.chat.events}")
    public void onChatEvent(
            String payload,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        try {
            log.info("rabbit chat event received payload={}, channel={}, deliveryTag={}", payload, channel, deliveryTag);
            processChatEventPayload(payload);
        } catch (JsonProcessingException e) {
            log.warn("RabbitMQ 채팅 이벤트 JSON 파싱 불가 — 재큐잉하지 않음: {}", e.getMessage());
            channel.basicNack(deliveryTag, false, false);
            return;
        } catch (Exception e) {
            log.error("RabbitMQ 채팅 이벤트 처리 실패 — 재큐잉: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
            return;
        }
        channel.basicAck(deliveryTag, false);
    }

    /**
     * DB 저장 등 부가 처리가 생기면 이 메서드 안에서 트랜잭션으로 끝낸 뒤 상위에서 basicAck 되도록 합니다.
     */
    private void processChatEventPayload(String payload) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(payload);
        log.info(
                "rabbit chat event received chatRoomId={} chatMessageId={}",
                root.path("chatRoomId").asText(),
                root.path("chatMessageId").asText()
        );
    }
}