package com.backend.onharu.infra.websocket;

import com.backend.onharu.application.ChatFacade;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import static com.backend.onharu.domain.chat.dto.ChatMessageCommand.*;

/**
 * 웹소켓 메시지 처리 컨트롤러 (STOMP 사용)
 */
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatFacade chatFacade;
    private final SimpMessagingTemplate messagingTemplate; // 메시지 구독 경로에 브로드 캐스팅 역할

    /**
     * 메시지 전송
     * @param request 채팅방 ID, 발신자 ID, 메시지 내용을 포함한 요청
     */
    @MessageMapping("/chat/send")
    public void sendMessage(ChatMessageRequest request) {

        // 채팅 메시지 생성 호출 및 응답 생성
        ChatMessageResponse response = chatFacade.createChatMessage(
                new CreateChatMessageCommand(
                        request.chatRoomId(),
                        request.senderId(),
                        request.content()
                )
        );

        // 해당 채팅방에 참여한 메시지 구독자에게 전송(브로드캐스팅)
        messagingTemplate.convertAndSend("/topic/chat/" + request.chatRoomId(), response);
    }
}
