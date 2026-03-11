package com.backend.onharu.domain.chat.dto;

import com.backend.onharu.domain.chat.model.ChatParticipant;
import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.user.model.User;

/**
 * 채팅참여자 Command DTO 입니다.
 */
public class ChatParticipantCommand {

    /**
     * 채팅참여자 생성 Command
     *
     * @param chatRoom 채팅방
     * @param user     사용자
     */
    public record CreateChatParticipantCommand(
            ChatRoom chatRoom,
            User user
    ) {
    }

    /**
     * 채팅참여자 업데이트 Command
     *
     * @param chatParticipant 채팅참여자 엔티티
     */
    public record updateChatParticipantCommand(
            ChatParticipant chatParticipant
    ) {
    }
}
