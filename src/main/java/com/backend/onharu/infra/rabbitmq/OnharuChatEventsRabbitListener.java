package com.backend.onharu.infra.rabbitmq;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 이벤트 큐 구독 — 후처리·워커 확장 전 로그 확인용.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "onharu.rabbitmq.enabled", havingValue = "true")
public class OnharuChatEventsRabbitListener {

    @RabbitListener(queues = "${onharu.rabbitmq.chat-events-queue:onharu.chat.events}")
    public void onChatEvent(String payload) {
        log.info("rabbit chat event received payload={}", payload);
    }
}