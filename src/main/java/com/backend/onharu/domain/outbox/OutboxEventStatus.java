package com.backend.onharu.domain.outbox;

/**
 * 트랜잭션 아웃박스 행 상태: 대기 → 브로커 전송 완료 또는 실패.
 */
public enum OutboxEventStatus {
    PENDING,
    SENT,
    FAILED
}
