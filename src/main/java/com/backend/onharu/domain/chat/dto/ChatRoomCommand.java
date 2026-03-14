package com.backend.onharu.domain.chat.dto;

import com.backend.onharu.domain.chat.model.ChatRoom;
import com.backend.onharu.domain.user.model.User;

import java.util.List;

/**
 * 채팅방 도메인의 Command DTO 입니다.
 */
public class ChatRoomCommand {

    /**
     * 채팅방 생성 Command
     * @param name 채팅방 이름
     * @param userId 채팅방 주인 사용자 ID
     * @param participantUserIds 채팅방 참가자 사용자 ID 목록
     */
    public record CreateChatRoomCommand(
            String name,
            Long userId,
            List<Long> participantUserIds
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
}
