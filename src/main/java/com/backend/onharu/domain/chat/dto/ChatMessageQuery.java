package com.backend.onharu.domain.chat.dto;

import org.springframework.data.domain.Pageable;

/**
 * 채팅 메시지 Query DTO 입니다.
 */
public class ChatMessageQuery {

    /**
     * 안읽은 메세지 갯수 세기 Query
     * @param chatRoomId 채팅방 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     */
    public record CountUnreadMessageQuery(
            Long chatRoomId,
            Long lastReadMessageId
    ) {
    }

    /**
     * 채팅 메시지 목록 조회 Query
     * @param chatRoomId 채팅방 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     * @param pageable 페이징 정보
     * @param userId 채팅 메시지를 읽은 유저
     */
    public record FindChatMessageQuery(
            Long chatRoomId,
            Long lastReadMessageId,
            Pageable pageable,
            Long userId
    ) {
    }
}
