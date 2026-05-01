package com.backend.onharu.infra.websocket;

import java.time.LocalDateTime;

/**
 * 메시지 전송 응답 DTO
 * @param chatMessageId 채팅메시지 ID
 * @param sender 발신자(메세지를 보낸 사용자 ID)
 * @param content 메시지 내용
 * @param createdAt 메시지 생성시간
 */
public record ChatMessageResponse(
        Long chatMessageId, // 채팅메시지 ID
        Long sender, // 발신자(메세지를 보낸 사용자 ID)
        String content, // 메시지 내용
        LocalDateTime createdAt // 메시지 생성시간
) {
}
