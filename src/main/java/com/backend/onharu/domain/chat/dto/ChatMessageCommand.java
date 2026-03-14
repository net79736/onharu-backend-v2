package com.backend.onharu.domain.chat.dto;

import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.user.model.User;

/**
 * 채팅메시지 도메인 Command DTO 입니다.
 */
public class ChatMessageCommand {

    /**
     * 채팅 메시지 생성 Command
     * @param chatRoomId 채팅방 ID
     * @param senderId 발신자 ID (메시지를 보내는 사용자 ID)
     * @param content 메시지 내용
     */
    public record CreateChatMessageCommand(
            Long chatRoomId,
            Long senderId,
            String content
    ) {
    }

    /**
     * 채팅 메시지 읽음 Command
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     * @param messageId 메시지 ID
     */
    public record ReadMessageCommand(
            Long chatRoomId,
            Long userId,
            Long messageId
    ) {
    }
}
