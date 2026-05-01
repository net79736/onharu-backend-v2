package com.backend.onharu.domain.chat.dto;

/**
 * 채팅방 도메인의 Query DTO 입니다.
 */
public class ChatRoomQuery {

    /**
     * 채팅방 ID 로 조회
     * @param chatRoomId 채팅방 ID
     */
    public record FindChatRoomByIdQuery(
            Long chatRoomId
    ) {
    }
}
