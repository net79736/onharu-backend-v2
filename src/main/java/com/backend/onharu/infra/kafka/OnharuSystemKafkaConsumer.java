package com.backend.onharu.infra.kafka;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 기본 토픽과 구분되는 시스템/아웃박스 알림용 두 번째 리스너(토픽·consumer group 분리).
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "onharu.kafka.enabled", havingValue = "true")
public class OnharuSystemKafkaConsumer {

    @KafkaListener(
            id = "onharuSystemKafkaConsumer",
            topics = "${onharu.kafka.system-topic:onharu-system-events}",
            groupId = "${onharu.kafka.system-consumer-group:onharu-system-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload String message) {
        log.info("kafka system consumer message={}", message);
    }
}
