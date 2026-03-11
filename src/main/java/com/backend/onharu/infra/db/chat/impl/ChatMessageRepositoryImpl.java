package com.backend.onharu.infra.db.chat.impl;

import com.backend.onharu.domain.chat.model.ChatMessage;
import com.backend.onharu.domain.chat.repository.ChatMessageRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.chat.ChatMessageJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.backend.onharu.domain.chat.dto.ChatMessageRepositoryParam.*;
import static com.backend.onharu.domain.support.error.ErrorType.Chat.CHAT_MESSAGE_NOT_FOUND;

@Repository
@RequiredArgsConstructor
public class ChatMessageRepositoryImpl implements ChatMessageRepository {

    private final ChatMessageJpaRepository chatMessageJpaRepository;

    @Override
    public void save(ChatMessage chatMessage) {
        chatMessageJpaRepository.save(chatMessage);
    }

    @Override
    public void delete(ChatMessage chatMessage) {
        chatMessageJpaRepository.delete(chatMessage);
    }

    @Override
    public ChatMessage findChatMessageById(FindChatMessageByIdParam param) {
        return chatMessageJpaRepository.findById(param.chatMessageId())
                .orElseThrow(() -> new CoreException(CHAT_MESSAGE_NOT_FOUND));
    }

    @Override
    public List<ChatMessage> findTopByChatRoom_IdOrderByIdDesc(FindTopByChatRoom_IdOrderByIdDescParam param) {
        return chatMessageJpaRepository.findTopByChatRoom_IdOrderByIdDesc(param.ChatRoomId());
    }

    @Override
    public long countUnreadMessage(CountUnreadMessageParam param) {
        return chatMessageJpaRepository.countUnreadMessage(param.chatRoomId(), param.lastReadMessageId());
    }

    @Override
    public List<ChatMessage> findChatMessage(FindChatMessageParam param) {
        return chatMessageJpaRepository.findChatMessage(param.chatRoomId(), param.cursorId(), param.pageable());
    }
}
