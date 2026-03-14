package com.backend.onharu.domain.chat.service;

import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.repository.ChatParticipantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.chat.dto.ChatParticipantCommand.CreateChatParticipantCommand;
import static com.backend.onharu.domain.chat.dto.ChatParticipantCommand.updateChatParticipantCommand;

@Service
@RequiredArgsConstructor
public class ChatParticipantCommandService {

    private final ChatParticipantRepository chatParticipantRepository;

    /**
     * 채팅참여자 생성
     *
     * @param command 채팅방 엔티티와 사용자 엔티티를 포함한 Command
     * @return 생성한 채팅참여자 엔티티
     */
    @Transactional
    public ChatParticipant createChatParticipant(CreateChatParticipantCommand command) {
        // 채팅참여자 객체 생성
        ChatParticipant chatParticipant = ChatParticipant.builder()
                .chatRoom(command.chatRoom())
                .user(command.user())
                .build();

        // 채팅참여자 DB 저장
        return chatParticipantRepository.save(chatParticipant);
    }

    public void updateChatParticipant(updateChatParticipantCommand command) {
        chatParticipantRepository.save(command.chatParticipant());
    }
}
