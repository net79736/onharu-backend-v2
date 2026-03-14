package com.backend.onharu.domain.chat.dto;

/**
 * 채팅방 Repository 에 사용될 DTO 입니다.
 */
public class ChatRoomRepositoryParam {

    /**
     * 채팅방 조회
     * @param chatRoomId 채팅방 ID
     */
    public record FindChatRoomByIdParam(
            Long chatRoomId
    ) {
    }
}
