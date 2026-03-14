package com.backend.onharu.domain.chat.service;

import com.backend.onharu.domain.chat.dto.ChatMessageCommand.CreateChatMessageCommand;
import com.backend.onharu.domain.chat.model.ChatMessage;
import com.backend.onharu.domain.chat.repository.ChatMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatMessageCommandService {

    private final ChatMessageRepository chatMessageRepository;

    @Transactional
    public ChatMessage createChatMessage(CreateChatMessageCommand command) {
        // 채팅메시지 객체 생성
        ChatMessage chatMessage = ChatMessage.builder()
                .content(command.content())
                .build();

        // 채팅메시지 저장
        chatMessageRepository.save(chatMessage);

        return chatMessage;
    }
}
