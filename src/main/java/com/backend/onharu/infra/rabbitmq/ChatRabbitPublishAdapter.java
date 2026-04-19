package com.backend.onharu.infra.rabbitmq;

import java.time.LocalDateTime;
import java.util.Map;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.backend.onharu.domain.event.ChatRabbitPublishPort;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅 이벤트 JSON 을 RabbitMQ 큐로 전송합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "onharu.rabbitmq.enabled", havingValue = "true")
public class ChatRabbitPublishAdapter implements ChatRabbitPublishPort {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @Value("${onharu.rabbitmq.chat-events-queue:onharu.chat.events}")
    private String chatEventsQueue; // RabbitMQ 채팅 이벤트 큐 이름. default: onharu.chat.events

    /**
     * 채팅 메시지 저장 직후 RabbitMQ 큐로 이벤트 전송
     * 
     * @param chatRoomId 채팅방 ID
     * @param chatMessageId 채팅 메시지 ID
     * @param senderId 채팅 메시지 발신자 ID
     * @param content 채팅 메시지 내용
     * @param createdAt 채팅 메시지 생성 시간
     */
    @Override
    public void publishChatMessagePublished(
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
            rabbitTemplate.convertAndSend("", chatEventsQueue, payload);
        } catch (JsonProcessingException e) {
            log.warn("RabbitMQ 채팅 이벤트 JSON 직렬화 실패: {}", e.getMessage());
        }
    }
}