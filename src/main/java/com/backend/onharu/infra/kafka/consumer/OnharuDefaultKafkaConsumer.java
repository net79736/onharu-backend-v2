package com.backend.onharu.infra.kafka.consumer;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.MessageHeaders;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

/**
 * coupon-issue-v3 의 {@code KafkaConsumer} 처럼 기본 토픽을 구독해 로그로 확인합니다.
 * (추후: 읽음 처리·알림·분석 등 비동기 후처리로 확장)
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "onharu.kafka.enabled", havingValue = "true")
public class OnharuDefaultKafkaConsumer {

    @KafkaListener(
            id = "onharuKafkaConsumer",
            topics = "${spring.kafka.template.default-topic}", // 기본 토픽
            groupId = "${spring.kafka.consumer.group-id}", // 기본 그룹 ID
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(@Payload String message, @Headers MessageHeaders headers) {
        log.info("kafka consumer message={} headers={}", message, headers);
    }
}
