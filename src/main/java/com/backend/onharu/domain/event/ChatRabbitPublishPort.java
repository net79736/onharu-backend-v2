package com.backend.onharu.domain.event;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 저장 직후 동일 페이로드를 RabbitMQ 로 보낼 때 사용하는 포트
 */
public interface ChatRabbitPublishPort {

    void publishChatMessagePublished(
            long chatRoomId,
            long chatMessageId,
            long senderId,
            String content,
            LocalDateTime createdAt
    );
}