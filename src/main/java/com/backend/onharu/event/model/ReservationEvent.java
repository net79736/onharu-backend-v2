package com.backend.onharu.event.model;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;

/**
 * 예약 이벤트 모델
 * 
 * 예약 상태 변경 시 발생하는 이벤트를 정의하는 모델
 * 
 * @param reservationId 예약 ID
 * @param ownerId 사장님 ID
 * @param childId 아동 ID
 * @param type 알림 유형
 */
public record ReservationEvent(
    Long reservationId,
    Long ownerId,
    Long childId,
    NotificationHistoryType type
) {
    
}
