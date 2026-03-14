package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.common.enums.RoomType;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 채팅 REST API 에 사용될 DTO 입니다.
 */
public class ChatControllerDto {

    /**
     * 채팅방 정보
     * @param chatRoomId 채팅방 ID
     * @param lastMessage 메시지 내용
     * @param lastMessageTime 메시지 생성 시각
     * @param unreadMessageCount 안읽은 메세지 수
     */
    public record ChatRoomResponse(
            Long chatRoomId,
            String lastMessage,
            LocalDateTime lastMessageTime,
            long unreadMessageCount,
            List<String> chatParticipants
    ) {
    }

    /**
     * 채팅방 정로 목록
     * @param chatRoomResponses 채팅방 정보
     */
    public record ChatRoomsResponse(
            List<ChatRoomResponse> chatRoomResponses
    ) {
    }

    /**
     * 채팅 메시지 정보
     * @param chatMessageId 채팅 메세지 ID
     * @param senderName 발신자 이름
     * @param content 메시지 내용
     * @param createdAt 메시지 작성 시각
     */
    public record ChatRoomMessageResponse(
            Long chatMessageId,
            String senderName,
            String content,
            LocalDateTime createdAt
    ) {
    }

    /**
     * 채팅 메시지 정보 목록
     * @param chatRoomMessageResponses 채팅 메시지 정보
     */
    public record ChatRoomMessagesResponse(
            List<ChatRoomMessageResponse> chatRoomMessageResponses
    ) {
    }

    /**
     * 채팅방 생성 요청
     * @param name 채팅방 이름
     * @param roomType 채팅방 타입(일대일 또는 그룹)
     * @param chatParticipantIds 채팅방 참여자 ID 목록
     */
    public record CreateChatRoomRequest(
            String name,
            RoomType roomType,
            List<Long> chatParticipantIds
    ) {
    }

    /**
     * 채팅방 생성 응답
     * @param chatRoomId 채팅방 ID
     */
    public record CreateChatRoomResponse(
            Long chatRoomId
    ) {
    }

    /**
     * 채팅방 초대 요청
     * @param userIds 사용자 ID 목록
     */
    public record InviteChatRoomRequest(
            List<Long> userIds
    ) {
    }

    /**
     * 채팅방 수정 요청
     * @param name 채팅방 이름
     */
    public record UpdateChatRoomRequest(
            String name
    ) {
    }

    /**
     * 채팅방 메시지 읽음 요청
     * @param messageId 메시지 ID
     */
    public record ReadMessageRequest(
            Long messageId
    ) {
    }
}
