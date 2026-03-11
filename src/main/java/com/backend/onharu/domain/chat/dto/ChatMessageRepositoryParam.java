package com.backend.onharu.domain.chat.dto;

import org.springframework.data.domain.Pageable;

/**
 * 채팅 메시지 Repository 에 사용될 DTO 입니다.
 */
public class ChatMessageRepositoryParam {

    /**
     * 채팅메시지 ID 로 채팅 메세지 조회
     *
     * @param chatMessageId 채팅 메시지
     */
    public record FindChatMessageByIdParam(
            Long chatMessageId
    ) {
    }

    /**
     * 특정 채팅방의 가장 마지막 채팅메세지 조회
     *
     * @param ChatRoomId 채팅방 ID
     */
    public record FindTopByChatRoom_IdOrderByIdDescParam(
            Long ChatRoomId
    ) {
    }

    /**
     * 안읽은 메시지 수 계산
     *
     * @param chatRoomId        채팅방 ID
     * @param lastReadMessageId 마지막으로 읽은 메시지 ID
     */
    public record CountUnreadMessageParam(
            Long chatRoomId,
            Long lastReadMessageId
    ) {
    }

    /**
     * 채팅 메시지 목록 조회
     *
     * @param chatRoomId        채팅방 ID
     * @param cursorId 마지막으로 읽은 메시지 ID
     * @param pageable 페이징 정보
     */
    public record FindChatMessageParam(
            Long chatRoomId,
            Long cursorId,
            Pageable pageable
    ) {
    }
}
