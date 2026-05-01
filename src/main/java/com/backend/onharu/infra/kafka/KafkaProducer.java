package com.backend.onharu.infra.kafka;

import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.kafka.support.SendResult;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 문자열 페이로드를 Kafka 로 전송합니다. {@code onharu.kafka.enabled=false} 이면 빈이 등록되지 않습니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "onharu.kafka.enabled", havingValue = "true")
public class KafkaProducer {

    private final KafkaTemplate<String, String> onharuKafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String defaultTopic;

    /**
     * 기본 토픽으로 문자열 페이로드를 Kafka 로 전송합니다.
     * defaultTopic: onharu-chat
     * @param payload
     */
    public void publish(String payload) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, defaultTopic)
                .build();
        // Kafka 메시지 전송 및 결과를 로깅합니다.
        sendAndLog(onharuKafkaTemplate.send(message));
    }

    public void publish(String topic, String payload) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
        // Kafka 메시지 전송 및 결과를 로깅합니다.
        sendAndLog(onharuKafkaTemplate.send(message));
    }

    /**
     * Kafka 메시지 전송 결과를 로깅합니다.
     * 
     * @param future
     */
    private static void sendAndLog(CompletableFuture<SendResult<String, String>> future) {
        future.whenComplete((result, ex) -> {
            if (ex == null) {
                log.info("kafka producer ok offset={} topic={}",
                        result.getRecordMetadata().offset(),
                        result.getRecordMetadata().topic());
            } else {
                log.warn("kafka producer failed: {}", ex.getMessage());
            }
        });
    }
}