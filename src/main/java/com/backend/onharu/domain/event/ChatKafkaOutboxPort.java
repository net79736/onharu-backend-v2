package com.backend.onharu.domain.event;

import java.time.LocalDateTime;

/**
 * 채팅 메시지 저장과 같은 DB 트랜잭션 안에서 Kafka 발행용 페이로드를 아웃박스에 적재합니다.
 * 구현체는 Kafka 활성 + {@code onharu.kafka.outbox.enabled} 일 때만 등록됩니다.
 */
public interface ChatKafkaOutboxPort {

    void enqueueChatMessagePublished(
            long chatRoomId,
            long chatMessageId,
            long senderId,
            String content,
            LocalDateTime createdAt
    );
}