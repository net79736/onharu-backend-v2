package com.backend.onharu.domain.chat.dto;

import java.util.List;

/**
 * 채팅참여자 Repository DTO 입니다.
 */
public class ChatParticipantRepositoryParam {

    /**
     * 채팅 참여자 단건 조회
     * @param chatParticipantId 채팅참여자 ID
     */
    public record FindChatParticipantByIdParam(
            Long chatParticipantId
    ) {
    }

    /**
     * 특정 채팅방의 채팅참여 사용자 조회
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public record FindByChatRoomIdAndUserIdParam(
            Long chatRoomId,
            Long userId
    ) {
    }

    /**
     * 내가 참여한 채팅방 목록 조회
     * @param userId 사용자 ID
     */
    public record GetChatRoomSummaryParam(
            Long userId
    ) {
    }

    /**
     * 정렬된 채팅방 목록 조회
     * @param userId 사용자 ID
     */
    public record FindSortedChatRoomsParam(
            Long userId
    ) {
    }

    /**
     * 특정 채팅방의 채팅 참가자 목록 조회
     * @param chatRoomIds 채팅방 ID 목록
     */
    public record FindChatParticipantByChatRoomIdParam(
            List<Long> chatRoomIds
    ) {
    }
}
