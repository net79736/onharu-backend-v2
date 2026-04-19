package com.backend.onharu.infra.kafka.outbox;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import com.backend.onharu.domain.event.ChatKafkaOutboxPort;
import com.backend.onharu.domain.outbox.OutboxEventStatus;
import com.backend.onharu.domain.outbox.model.OutboxEvent;
import com.backend.onharu.domain.outbox.repository.OutboxEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 메시지 JSON을 아웃박스 DB 테이블에 적재합니다. 릴레이 스케줄러가 Kafka 로 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnExpression(
        "'${onharu.kafka.enabled:false}'.equals('true') && '${onharu.kafka.outbox.enabled:true}'.equals('true')"
)
public class ChatKafkaOutboxAdapter implements ChatKafkaOutboxPort {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;

    @Value("${spring.kafka.template.default-topic}")
    private String defaultChatTopic; // onharu-chat

    /**
     * 채팅 메시지 JSON을 아웃박스 DB 테이블에 적재합니다. 릴레이 스케줄러가 Kafka 로 전송합니다.
     * defaultChatTopic: onharu-chat
     * 
     * @param chatRoomId 채팅방 ID
     * @param chatMessageId 채팅 메시지 ID
     * @param senderId 채팅 메시지 발신자 ID
     * @param content 채팅 메시지 내용
     * @param createdAt 채팅 메시지 생성 시간
     */
    @Override
    public void enqueueChatMessagePublished(
            long chatRoomId,
            long chatMessageId,
            long senderId,
            String content,
            LocalDateTime createdAt
    ) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "chatRoomId", chatRoomId,
                    "chatMessageId", chatMessageId,
                    "senderId", senderId,
                    "content", content,
                    "createdAt", createdAt.toString()
            ));
            OutboxEvent row = new OutboxEvent(defaultChatTopic, payload, OutboxEventStatus.PENDING);
            outboxEventRepository.save(row);
        } catch (JsonProcessingException e) {
            log.warn("채팅 아웃박스 JSON 직렬화 실패: {}", e.getMessage());
        }
    }
}
