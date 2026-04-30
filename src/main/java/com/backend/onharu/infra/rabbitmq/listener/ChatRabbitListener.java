package com.backend.onharu.infra.rabbitmq.listener;

import java.io.IOException;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 이벤트 큐 구독 — 수동 ACK 로 처리 완료 후에만 큐에서 제거합니다.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "onharu.rabbitmq.enabled", havingValue = "true")
public class ChatRabbitListener {

    @RabbitListener(queues = "${onharu.rabbitmq.chat-events-queue:onharu.chat.events}")
    public void onChatEvent(
            String payload,
            Channel channel,
            @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag
    ) throws IOException {
        try {
            // 브로드캐스트(SYSTEM_BROADCAST) 처리 경로는 제거했습니다.
            // 현재는 레거시 이벤트 큐를 "수신 확인 + 로그" 수준으로만 유지합니다.
            log.info("RabbitMQ 채팅 이벤트 수신 payloadLength={}", payload == null ? 0 : payload.length());
        } catch (Exception e) {
            log.error("RabbitMQ 채팅 이벤트 처리 실패 — 재큐잉: {}", e.getMessage(), e);
            channel.basicNack(deliveryTag, false, true);
            return;
        }
        channel.basicAck(deliveryTag, false);
    }
}

