package com.backend.onharu.infra.db.chat.impl;

import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatParticipantRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.chat.ChatParticipantJpaRepository;
import com.backend.onharu.infra.db.chat.ChatRoomSummary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.backend.onharu.domain.chat.dto.ChatParticipantRepositoryParam.*;
import static com.backend.onharu.domain.support.error.ErrorType.Chat.CHAT_PARTICIPANTS_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class ChatParticipantRepositoryImpl implements ChatParticipantRepository {

    private final ChatParticipantJpaRepository chatParticipantJpaRepository;

    @Override
    public ChatParticipant save(ChatParticipant chatParticipant) {
        return chatParticipantJpaRepository.save(chatParticipant);
    }

    @Override
    public void delete(ChatParticipant chatParticipant) {
        chatParticipantJpaRepository.delete(chatParticipant);
    }

    @Override
    public ChatParticipant findById(FindChatParticipantByIdParam param) {
        return chatParticipantJpaRepository.findById(param.chatParticipantId())
                .orElseThrow(() -> new CoreException(CHAT_PARTICIPANTS_NOT_FOUND));
    }

    @Override
    public ChatParticipant findByChatRoomIdAndUserId(FindByChatRoomIdAndUserIdParam param) {
        return chatParticipantJpaRepository.findByChatRoom_IdAndUser_Id(param.chatRoomId(), param.userId())
                .orElseThrow(() -> new CoreException(CHAT_PARTICIPANTS_NOT_FOUND));
    }

    @Override
    public List<ChatRoomSummary> getChatRoomSummary(GetChatRoomSummaryParam param) {
        return chatParticipantJpaRepository.getChatRoomSummary(param.userId());
    }

    @Override
    public List<ChatRoom> findSortedChatRooms(FindSortedChatRoomsParam param) {
        return chatParticipantJpaRepository.findSortedChatRooms(param.userId());
    }
}
