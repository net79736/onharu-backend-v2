package com.backend.onharu.domain.common.enums;

/**
 * 알림 히스토리 유형을 정의하는 enum
 *
 * 예약 관련:
 * - RESERVATION_CREATED: 예약 생성
 * - RESERVATION_CONFIRMED: 예약 확정
 * - RESERVATION_CANCELED: 예약 취소
 * - RESERVATION_COMPLETED: 예약 완료
 * - RESERVATION_REJECTED: 예약 거절
 */
public enum NotificationHistoryType {
    RESERVATION_CREATED,
    RESERVATION_CONFIRMED,
    RESERVATION_CANCELED,
    RESERVATION_COMPLETED,
    RESERVATION_REJECTED,
}