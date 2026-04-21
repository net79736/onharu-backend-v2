package com.backend.onharu.infra.kafka.outbox;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.outbox.model.OutboxEvent;
import com.backend.onharu.domain.outbox.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * PENDING 아웃박스 행을 Kafka 로 전송하고 상태를 갱신합니다. 행마다 별도 트랜잭션으로 커밋합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnExpression(
        "'${onharu.kafka.enabled:false}'.equals('true') && '${onharu.kafka.outbox.enabled:true}'.equals('true')"
)
public class OutboxRelayProcessor {

    private static final String OUTBOX_RELAYED_EVENT_TYPE = "OUTBOX_RELAYED";
    private static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private final OutboxEventRepository outboxEventRepository;
    private final KafkaTemplate<String, String> onharuKafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${onharu.kafka.system-topic:onharu-system-events}")
    private String systemTopic; // onharu-system-events

    /**
     * 아웃박스 행을 Kafka 로 전송하고 상태를 갱신합니다.
     * 
     * @param outboxEventId
     * @throws ExecutionException
     * @throws InterruptedException
     * @throws TimeoutException
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void relayOne(long outboxEventId) throws ExecutionException, InterruptedException, TimeoutException {
        OutboxEvent row = outboxEventRepository.getById(outboxEventId);
        // 아웃박스 행 상태가 대기 중이 아니면 릴레이하지 않습니다.
        if (!row.isPending()) {
            return;
        }
        // 아웃박스 행을 Kafka 로 전송합니다.
        onharuKafkaTemplate.send(row.getTargetTopic(), row.getPayload())
                .get(30, TimeUnit.SECONDS);
        // 아웃박스 행 상태를 브로커 전송 완료로 변경합니다.
        row.markSent(LocalDateTime.now(KST));
        // 아웃박스 행 상태를 저장합니다.
        outboxEventRepository.save(row);
    }

    /**
     * 채팅 토픽 전송 후 시스템 토픽에 릴레이 알림을 best-effort 로 보냅니다(두 번째 컨슈머 데모용).
     */
    public void notifySystemTopic(long outboxEventId) {
        try {
            String body = objectMapper.writeValueAsString(Map.of(
                    "type", OUTBOX_RELAYED_EVENT_TYPE,
                    "outboxEventId", outboxEventId
            ));
            onharuKafkaTemplate.send(systemTopic, body).get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("시스템 토픽 알림 실패 (인터럽트) outboxEventId={}: {}", outboxEventId, e.getMessage());
        } catch (JsonProcessingException | ExecutionException | TimeoutException e) {
            log.warn("시스템 토픽 알림 실패 outboxEventId={}: {}", outboxEventId, e.getMessage());
        }
    }

    /**
     * 아웃박스 행 상태를 FAILED 로 변경합니다.
     * 
     * @param outboxEventId
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markFailed(long outboxEventId) {
        outboxEventRepository.findById(outboxEventId).ifPresent(row -> {
            // 아웃박스 행 상태가 대기 중이면 상태를 FAILED 로 변경합니다.
            if (row.isPending()) {
                row.markFailed();
                outboxEventRepository.save(row);
            }
        });
    }
}
