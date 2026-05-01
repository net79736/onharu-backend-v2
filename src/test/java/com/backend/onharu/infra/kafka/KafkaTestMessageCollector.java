package com.backend.onharu.infra.kafka;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * 통합 테스트 전용: 프로덕션 {@link OnharuDefaultKafkaConsumer} 에 테스트용 static 을 두지 않고,
 * 동일 토픽·별도 {@code groupId} 로 수신 내역만 검증합니다(그룹이 다르면 브로커가 동일 메시지를 각각 전달).
 */
@Component
class KafkaTestMessageCollector {

    private static final CopyOnWriteArrayList<String> RECEIVED = new CopyOnWriteArrayList<>();

    static List<String> receivedMessages() {
        return List.copyOf(RECEIVED);
    }

    static void clearReceivedMessages() {
        RECEIVED.clear();
    }

    @KafkaListener(
            id = "kafkaTestMessageCollector",
            topics = "${spring.kafka.template.default-topic}",
            groupId = "${spring.kafka.consumer.group-id}-it-verifier",
            containerFactory = "kafkaListenerContainerFactory"
    )
    void collect(@Payload String message) {
        RECEIVED.add(message);
    }
}
