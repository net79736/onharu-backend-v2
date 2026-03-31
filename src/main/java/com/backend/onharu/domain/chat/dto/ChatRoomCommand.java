package com.backend.onharu.domain.chat.dto;

import com.backend.onharu.domain.common.enums.RoomType;

import java.util.List;

/**
 * 채팅방 도메인의 Command DTO 입니다.
 */
public class ChatRoomCommand {

    /**
     * 채팅방 생성 Command
     * @param name 채팅방 제목
     * @param roomType 채팅방 타입
     * @param targetId 채팅방 참가 대상 사용자 ID
     * @param userId 채팅방을 생성하는 사용자 ID
     */
    public record CreateChatRoomCommand(
            String name,
            RoomType roomType,
            Long targetId,
            Long userId
    ) {
    }

    /**
     * 채팅방 초대
     * @param chatRoomId 채팅방 ID
     * @param userIds 사용자 ID 목록
     */
    public record InviteChatRoomCommand(
            Long chatRoomId,
            List<Long> userIds
    ) {
    }

    /**
     * 채팅방 수정 Command
     * @param chatRoomId 채팅방 ID
     * @param name 채팅방 이름
     */
    public record UpdateChatRoomCommand(
            Long chatRoomId,
            String name
    ) {
    }

    /**
     * 채팅방 탈퇴 Command
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public record LeaveChatRoomCommand(
            Long chatRoomId,
            Long userId
    ) {
    }

    /**
     * 채팅방 입장 Command
     * @param chatRoomId 채팅방 ID
     * @param userId 사용자 ID
     */
    public record EnterChatRoomCommand(
            Long chatRoomId,
            Long userId
    ) {
    }
}
