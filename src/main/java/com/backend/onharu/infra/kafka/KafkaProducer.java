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

import com.backend.onharu.domain.event.EventPublisher;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * coupon-issue-v3 의 {@code KafkaProducer} 와 동일하며, {@link EventPublisher} 를 구현합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "onharu.kafka.enabled", havingValue = "true")
public class KafkaProducer implements EventPublisher {

    private final KafkaTemplate<String, String> onharuKafkaTemplate;

    @Value("${spring.kafka.template.default-topic}")
    private String defaultTopic;

    @Override
    public void publish(String payload) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, defaultTopic)
                .build();
        sendAndLog(onharuKafkaTemplate.send(message));
    }

    @Override
    public void publish(String topic, String payload) {
        Message<String> message = MessageBuilder
                .withPayload(payload)
                .setHeader(KafkaHeaders.TOPIC, topic)
                .build();
        sendAndLog(onharuKafkaTemplate.send(message));
    }

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