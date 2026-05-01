package com.backend.onharu.infra.kafka;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * {@link OnharuSystemKafkaConsumer} 와 동일 토픽을 별도 consumer group 으로 구독해 통합 테스트에서 수신 검증.
 */
@Component
class KafkaSystemTestMessageCollector {

    private static final CopyOnWriteArrayList<String> RECEIVED = new CopyOnWriteArrayList<>();

    static List<String> receivedMessages() {
        return List.copyOf(RECEIVED);
    }

    static void clearReceivedMessages() {
        RECEIVED.clear();
    }

    @KafkaListener(
            id = "kafkaSystemTestMessageCollector",
            topics = "${onharu.kafka.system-topic:onharu-system-events}",
            groupId = "${onharu.kafka.system-consumer-group:onharu-system-group}-it-verifier",
            containerFactory = "kafkaListenerContainerFactory"
    )
    void collect(@Payload String message) {
        RECEIVED.add(message);
    }
}
