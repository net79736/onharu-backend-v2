package com.backend.onharu.event;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 예약 이벤트별 알림 메시지를 정의하는 enum입니다.
 *
 * 이벤트 타입(NotificationHistoryType)에 따라 가게 사장용·아동용 메시지 반환
 */
@Getter
@RequiredArgsConstructor
public enum ReservationNotificationMessage {

    RESERVATION_CREATED(
        "새로운 예약이 확정되었습니다. 예약 번호: %d",
        null
    ),
    RESERVATION_CONFIRMED(
        "예약이 확정되었습니다. 매장에서 만나요.",
        null
    ),
    RESERVATION_CANCELED(
        "예약이 취소되었습니다.",
        "예약 취소가 잘 처리됐어요. 다음에 또 봐요!"
    ),
    RESERVATION_COMPLETED(
        "예약이 완료됐어요! 매장에서 만나요.",
        "예약이 완료됐어요! 매장에서 만나요."
    ),
    RESERVATION_REJECTED(
        "예약이 거절되었습니다.",
        "미안해요, 이번 예약은 사정상 어려워졌어요."
    );

    private final String ownerMessageTemplate;
    private final String childMessage;

    /**
     * NotificationHistoryType에서 대응하는 메시지 enum 반환
     */
    public static ReservationNotificationMessage from(NotificationHistoryType type) {
        return valueOf(type.name());
    }

    /**
     * 가게 사장용 메시지 반환
     */
    public String getOwnerMessage(Long reservationId) {
        if (ownerMessageTemplate != null && ownerMessageTemplate.contains("%d")) {
            return String.format(ownerMessageTemplate, reservationId != null ? reservationId : 0L);
        }
        return ownerMessageTemplate;
    }

    /**
     * 아동용 메시지를 반환합니다.
     */
    public String getChildMessage() {
        return childMessage;
    }
}
