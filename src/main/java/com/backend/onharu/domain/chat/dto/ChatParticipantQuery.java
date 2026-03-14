package com.backend.onharu.domain.chat.dto;

/**
 * 채팅참여자와 Query DTO 입니다.
 */
public class ChatParticipantQuery {

    /**
     * 내가 참여한 채팅방 목록 조회
     * @param userId 사용자 ID
     */
    public record GetChatRoomSummaryQuery(
            Long userId
    ) {
    }

    /**
     * 채팅방 참가자 조회
     * @param ChatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public record GetChatParticipantQuery(
            Long ChatRoomId,
            Long userId
    ) {
    }
}
