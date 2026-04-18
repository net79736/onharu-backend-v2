package com.backend.onharu.infra.kafka;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
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

import com.backend.onharu.domain.event.EventPublisher;

/**
 * coupon-issue-v3 {@code KafkaIntegrationTest} / movie {@code @EmbeddedKafka} 패턴과 동일한 축:
 * 임베디드 브로커로 발행→구독까지 검증합니다.
 * 수신 검증은 {@link KafkaTestMessageCollector}(테스트 전용 리스너, 별도 consumer group)로 합니다.
 */
@SpringBootTest
@Import(KafkaTestMessageCollector.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@EmbeddedKafka(
        partitions = 1,
        topics = {KafkaIntegrationTest.DEFAULT_TOPIC}
)
@TestPropertySource(
        properties = {
                "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
                "onharu.kafka.enabled=true",
                "spring.kafka.listener.auto-startup=true",
                "spring.kafka.template.default-topic=onharu-chat",
                "spring.kafka.consumer.group-id=onharu-embedded-test",
                "spring.kafka.consumer.auto-offset-reset=earliest",
                "spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer",
                "spring.kafka.consumer.key-deserializer=org.apache.kafka.common.serialization.StringDeserializer",
                "spring.kafka.consumer.value-deserializer=org.apache.kafka.common.serialization.StringDeserializer"
        }
)
@DisplayName("Kafka Embedded 통합 테스트")
class KafkaIntegrationTest {

    static final String DEFAULT_TOPIC = "onharu-chat";

    @Autowired
    private EventPublisher eventPublisher;

    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void setUp() {
        KafkaTestMessageCollector.clearReceivedMessages();
        await()
                .atMost(60, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> {
                    for (String id : new String[] {"onharuKafkaConsumer", "kafkaTestMessageCollector"}) {
                        MessageListenerContainer c = kafkaListenerEndpointRegistry.getListenerContainer(id);
                        assertThat(c).isNotNull();
                        assertThat(c.isRunning()).isTrue();
                    }
                });
    }

    @Test
    @DisplayName("기본 토픽 publish 와 명시 토픽 publish 모두 컨슈머가 수신한다")
    void publish_defaultAndExplicitTopic_deliveredToListener() {
        String payloadDefault = "embedded-default-" + System.nanoTime();
        eventPublisher.publish(payloadDefault);
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertThat(KafkaTestMessageCollector.receivedMessages()).contains(payloadDefault));

        KafkaTestMessageCollector.clearReceivedMessages();

        String payloadExplicit = "embedded-explicit-" + System.nanoTime();
        eventPublisher.publish(DEFAULT_TOPIC, payloadExplicit);
        await()
                .atMost(30, TimeUnit.SECONDS)
                .pollInterval(Duration.ofMillis(100))
                .untilAsserted(() -> assertThat(KafkaTestMessageCollector.receivedMessages()).contains(payloadExplicit));
    }
}