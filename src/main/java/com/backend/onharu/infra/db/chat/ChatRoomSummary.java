package com.backend.onharu.infra.db.chat;

import java.time.LocalDateTime;

public interface ChatRoomSummary {
    Long getId(); // 채팅방 ID
    String getContent(); // 마지막 메시지 내용
    LocalDateTime getCreatedAt(); // 마지막 메시지 작성 시간
    Long getLastReadMessageId(); // 안읽은 메시지 수
}
