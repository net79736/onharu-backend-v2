package com.backend.onharu.infra.db.chat.impl;

import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatRoomRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.chat.ChatRoomJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import static com.backend.onharu.domain.chat.dto.ChatRoomRepositoryParam.FindChatRoomByIdParam;
import static com.backend.onharu.domain.support.error.ErrorType.Chat.CHAT_ROOM_NOT_FOUND;

/**
 * 채팅방 도메인 Repository 의 구현체
 */
@Repository
@RequiredArgsConstructor
public class ChatRoomRepositoryImpl implements ChatRoomRepository {

    private final ChatRoomJpaRepository chatRoomJpaRepository;

    @Override
    public ChatRoom save(ChatRoom chatRoom) {
        return chatRoomJpaRepository.save(chatRoom);
    }

    @Override
    public void delete(ChatRoom chatRoom) {
        chatRoomJpaRepository.delete(chatRoom);
    }

    @Override
    public Page<ChatRoom> findAll(Pageable pageable) {
        return chatRoomJpaRepository.findAll(pageable);
    }

    @Override
    public ChatRoom findById(FindChatRoomByIdParam param) {
        return chatRoomJpaRepository.findById(param.chatRoomId())
                .orElseThrow(() -> new CoreException(CHAT_ROOM_NOT_FOUND));
    }
}
