package com.backend.onharu.infra.kafka.outbox;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.backend.onharu.domain.outbox.OutboxEventStatus;
import com.backend.onharu.domain.outbox.model.OutboxEvent;
import com.backend.onharu.domain.outbox.repository.OutboxEventRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주기적으로 PENDING 아웃박스를 브로커로 넘깁니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression(
        "'${onharu.kafka.enabled:false}'.equals('true') && '${onharu.kafka.outbox.enabled:true}'.equals('true')"
)
public class OutboxRelayScheduler {

    private final OutboxEventRepository outboxEventRepository;
    private final OutboxRelayProcessor outboxRelayProcessor;

    @Scheduled(fixedDelayString = "${onharu.kafka.outbox.relay-delay-ms:5000}")
    public void tick() {
        // 상태가 대기 중인 아웃박스 행을 최대 50개 조회합니다.
        List<OutboxEvent> pending = outboxEventRepository.findTop50ByStatusOrderByIdAsc(OutboxEventStatus.PENDING);
        // 아웃박스 리스트 순회
        for (OutboxEvent row : pending) {
            long id = row.getId(); // 아웃박스 행 ID
            try {
                outboxRelayProcessor.relayOne(id); // 아웃박스 행을 Kafka 로 전송
                outboxRelayProcessor.notifySystemTopic(id); // 시스템 토픽에 릴레이 알림을 best-effort 로 보냅니다(두 번째 컨슈머 데모용).
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("아웃박스 릴레이 중단 id={}", id);
                outboxRelayProcessor.markFailed(id);
            } catch (ExecutionException | TimeoutException e) {
                log.warn("아웃박스 릴레이 실패 id={}: {}", id, e.getMessage());
                outboxRelayProcessor.markFailed(id);
            } catch (RuntimeException e) {
                log.warn("아웃박스 릴레이 실패 id={}: {}", id, e.getMessage());
                outboxRelayProcessor.markFailed(id);
            }
        }
    }
}
