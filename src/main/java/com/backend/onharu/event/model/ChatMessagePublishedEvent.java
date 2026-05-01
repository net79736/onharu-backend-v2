package com.backend.onharu.event.model;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 발행 이벤트 — 메인 트랜잭션 커밋 후에만 처리해야 할 부수 작업(채팅방 마지막 메시지 갱신, 외부 브로커 발행 등)을 트리거합니다.
 * 동일 chatRoomId 에 대한 동시 쓰기가 메인 트랜잭션에 남아 있으면 chat_messages INSERT 의 FK S-lock 과 chat_rooms UPDATE 의 X-lock 이
 * 충돌해 데드락이 발생하므로, 해당 쓰기 경로를 별도 트랜잭션(AFTER_COMMIT + REQUIRES_NEW)으로 분리하기 위한 이벤트입니다.
 */
public record ChatMessagePublishedEvent(
        long chatRoomId,
        long chatMessageId,
        long senderId,
        String content,
        LocalDateTime createdAt
) {
}
