package com.backend.onharu.infra.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.support.TransactionTemplate;

import com.backend.onharu.domain.event.ChatKafkaOutboxPort;

/**
 * 트랜잭션 아웃박스: DB 커밋된 PENDING 행이 릴레이에 의해 채팅 토픽·시스템 토픽으로 전달되는지 검증합니다.
 */
@SpringBootTest
@Import({KafkaTestMessageCollector.class, KafkaSystemTestMessageCollector.class})
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
        partitions = 1,
        topics = {KafkaIntegrationTest.DEFAULT_TOPIC, "onharu-system-events"}
)
@TestPropertySource(
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "onharu.kafka.enabled=true",
                "onharu.kafka.outbox.enabled=true",
                "onharu.kafka.outbox.relay-delay-ms=200",
                "spring.kafka.listener.auto-startup=true",
                "spring.kafka.template.default-topic=onharu-chat",
                "spring.kafka.consumer.group-id=onharu-embedded-test",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "onharu.kafka.system-topic=onharu-system-events",
                "onharu.kafka.system-consumer-group=onharu-system-embedded-test"
        }
)
@DisplayName("Kafka 트랜잭션 아웃박스 릴레이")
class OutboxRelayIntegrationTest {

    @Autowired
    private ChatKafkaOutboxPort chatKafkaOutboxPort;

    @Autowired
    private TransactionTemplate transactionTemplate;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void setUp() {
        KafkaTestMessageCollector.clearReceivedMessages();
        KafkaSystemTestMessageCollector.clearReceivedMessages();
        await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    for (String id : new String[] {
                            "onharuKafkaConsumer",
                            "kafkaTestMessageCollector",
                            "onharuSystemKafkaConsumer",
                            "kafkaSystemTestMessageCollector"
                    }) {
                        MessageListenerContainer c = kafkaListenerEndpointRegistry.getListenerContainer(id);
                        assertThat(c).isNotNull();
                        assertThat(c.isRunning()).isTrue();
                    }
                });
    }

    @Test
    @DisplayName("아웃박스 적재 후 릴레이가 채팅 토픽과 시스템 토픽으로 전달한다")
    void pendingOutbox_relayedToChatAndSystemTopic() {
        String marker = "outbox-it-" + System.nanoTime();
        transactionTemplate.executeWithoutResult(status -> chatKafkaOutboxPort.enqueueChatMessagePublished(
                99L,
                100L,
                101L,
                marker,
                LocalDateTime.now()
        ));

        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertThat(KafkaTestMessageCollector.receivedMessages()).anyMatch(s -> s.contains(marker)));

        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertThat(KafkaSystemTestMessageCollector.receivedMessages())
                        .anyMatch(s -> s.contains("OUTBOX_RELAYED") && s.contains("\"outboxEventId\"")));
    }
}
