package com.backend.onharu.infra.websocket;

import java.util.Map;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.backend.onharu.application.ChatFacade;
import com.backend.onharu.domain.chat.dto.ChatMessageCommand.CreateChatMessageCommand;
import com.backend.onharu.domain.event.ChatKafkaOutboxPort;
import com.backend.onharu.domain.support.ChatStompDestination;
import com.backend.onharu.infra.kafka.producer.KafkaProducer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 웹소켓 메시지 처리 컨트롤러 (STOMP 사용)
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatMessageStompHandler {

    private final ChatFacade chatFacade;
    private final SimpMessagingTemplate messagingTemplate; // 메시지 구독 경로에 브로드 캐스팅 역할
    private final ObjectProvider<KafkaProducer> kafkaProducer;
    /** 아웃박스 사용 시 채팅 Kafka 적재는 DB 트랜잭션 안에서만 처리하고, 여기서는 직접 발행하지 않습니다. */
    private final ObjectProvider<ChatKafkaOutboxPort> chatKafkaOutboxPort;
    private final ObjectMapper objectMapper;

    /**
     * [실제 메시지 배달 로직]
     * 클라이언트가 "/app/chat/send"로 메시지를 보내면, 이 메서드가 실행됩니다.
     */
    @MessageMapping(ChatStompDestination.MESSAGE_MAPPING_CHAT_SEND)
    public void sendMessage(ChatMessageRequest request) {
        log.info("메시지 전송 요청 발생: {}", request);

        // 2. [비즈니스 로직] 데이터베이스(DB)에 저장하고, 화면에 보여줄 예쁜 데이터를 만듭니다.
        // "누가, 어느 방에, 어떤 내용"을 썼는지 DB에 기록하는 역할을 합니다.
        ChatMessageResponse response = chatFacade.createChatMessage(
                new CreateChatMessageCommand(
                    request.chatRoomId(), // 어느 채팅방인지
                    request.senderId(),   // 누가 보냈는지
                    request.content()     // 뭐라고 했는지
                )
        );

        // 3. [배달/방송] 처리가 끝난 메시지를 해당 채팅방을 '구독' 중인 모든 사람에게 던집니다.
        // 주소 앞에 "/topic"이 붙었죠? 아까 설정한 '브로커'가 이 주소를 보고 
        // "아! /topic/chat/방번호 주소를 듣고 있는 모든 사람한테 다 보내줄게!" 하고 실시간 배달을 완료합니다.
        messagingTemplate.convertAndSend(
            ChatStompDestination.topicChatRoom(request.chatRoomId()), // "/topic/chat/{chatRoomId}"
            response
        );

        // 4. Kafka: 아웃박스 활성 시 Facade 에서 이미 적재했으므로 여기서는 직접 발행하지 않습니다.
        // 아웃박스 비활성 + Kafka 활성 시에만 기존 즉시 발행 경로를 씁니다.
        if (chatKafkaOutboxPort.getIfAvailable() == null) {
            kafkaProducer.ifAvailable(producer -> this.publishChatEvent(producer, request, response));
        }
    }

    /**
     * STOMP 처리 직후 동일 내용을 Kafka 로 비동기 적재
     */
    private void publishChatEvent(KafkaProducer producer, ChatMessageRequest request, ChatMessageResponse response) {
        try {
            String payload = objectMapper.writeValueAsString(Map.of(
                    "chatRoomId", request.chatRoomId(),
                    "chatMessageId", response.chatMessageId(),
                    "senderId", response.sender(),
                    "content", response.content(),
                    "createdAt", response.createdAt().toString()
            ));
            producer.publish(payload);
        } catch (JsonProcessingException e) {
            log.warn("Kafka 채팅 이벤트 JSON 직렬화 실패: {}", e.getMessage());
        }
    }
}
