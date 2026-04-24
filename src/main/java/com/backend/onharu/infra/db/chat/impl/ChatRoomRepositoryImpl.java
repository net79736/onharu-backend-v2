package com.backend.onharu.infra.db.chat.impl;

import static com.backend.onharu.domain.support.error.ErrorType.Chat.CHAT_ROOM_NOT_FOUND;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.chat.dto.ChatRoomRepositoryParam.FindChatRoomByIdParam;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.chat.repository.ChatRoomRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.chat.ChatRoomJpaRepository;

import lombok.RequiredArgsConstructor;

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

    /**
     * 채팅방의 마지막 메시지 ID를 최신 값으로 업데이트합니다.
     * [주의] 일반적인 JPA Dirty Checking을 쓰지 않는 이유:
     * 메세지 INSERT와 채팅방 UPDATE가 겹칠 때 발생하는 '데드락(Deadlock)'을 방지하기 위함입니다.
     * 
     * @param messageId 반드시 기존보다 큰 값이어야 갱신됨 (단조 증가 보장)
     * @return 갱신 성공 시 1, 기존 값이 더 크거나 대상이 없으면 0
     */
    @Override
    public int bumpLastMessageId(Long chatRoomId, Long messageId) {
        return chatRoomJpaRepository.bumpLastMessageId(chatRoomId, messageId);
    }
}
