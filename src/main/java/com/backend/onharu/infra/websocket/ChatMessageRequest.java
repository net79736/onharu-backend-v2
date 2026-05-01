package com.backend.onharu.infra.websocket;

/**
 * 메시지 전송 요청 DTO
 * @param chatRoomId 채팅방 ID
 * @param senderId 발신자(메세지를 보낸 사용자 ID)
 * @param content 메시지 내용
 */
public record ChatMessageRequest(
        Long chatRoomId,
        Long senderId,
        String content
) {
}
