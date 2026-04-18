package com.backend.onharu.domain.outbox.model;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.outbox.OutboxEventStatus;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Kafka 로 전달할 페이로드를 DB에 먼저 남기는 트랜잭션 아웃박스 행.
 * 비즈니스 저장과 동일 트랜잭션에서 INSERT 되고, 릴레이가 브로커로 전송 후 상태를 갱신합니다.
 */
@Table(name = "outbox_events")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class OutboxEvent extends BaseEntity {

    @Column(name = "TARGET_TOPIC", nullable = false, length = 255)
    private String targetTopic; // default: onharu-chat

    @Column(name = "PAYLOAD", nullable = false, columnDefinition = "TEXT")
    private String payload; // JSON 문자열

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 32)
    private OutboxEventStatus status; // PENDING, SENT, FAILED

    @Column(name = "SENT_AT")
    private LocalDateTime sentAt;

    public OutboxEvent(String targetTopic, String payload, OutboxEventStatus status) {
        this.targetTopic = targetTopic;
        this.payload = payload;
        this.status = status;
    }

    /**
     * 브로커 전송 완료 시 상태를 SENT 로 변경합니다.
     * @param at
     */
    public void markSent(LocalDateTime at) {
        this.status = OutboxEventStatus.SENT;
        this.sentAt = at;
    }

    /**
     * 브로커 전송 실패 시 상태를 FAILED 로 변경합니다.
     */
    public void markFailed() {
        this.status = OutboxEventStatus.FAILED;
    }
}
