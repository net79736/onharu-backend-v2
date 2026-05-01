package com.backend.onharu.domain.common.enums;

/**
 * 예약 상태를 정의하는 enum입니다.
 * 시스템에서 사용할 수 있는 예약 상태를 나타냅니다.
 * 
 * 예약 상태:
 * - WAITING: 대기 상태
 * - CONFIRMED: 확인 상태
 * - CANCELED: 취소 상태
 * - COMPLETED: 완료 상태
 */
public enum ReservationType {
    WAITING,
    CONFIRMED,
    CANCELED,
    COMPLETED
}
