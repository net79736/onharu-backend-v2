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
    private final String childMessageTemplate;

    /**
     * NotificationHistoryType에서 대응하는 메시지 enum 반환
     */
    public static ReservationNotificationMessage from(NotificationHistoryType type) {
        return valueOf(type.name());
    }

    /**
     * 가게 사장용 메시지 반환
     * 템플릿에 %d가 있으면 예약 번호로 치환 후, storeName이 있으면 "[storeName] 본문" 형식으로 반환
     */
    public String getOwnerMessageTemplate(String storeName, Long reservationId) {
        // 템플릿에 %d가 있으면 예약 번호로 치환
        String body = (ownerMessageTemplate != null && ownerMessageTemplate.contains("%d"))
            ? String.format(ownerMessageTemplate, reservationId != null ? reservationId : 0L)
            : ownerMessageTemplate;
        // storeName이 없으면 가게 이름 메시지 제거
        if (storeName == null || storeName.isBlank()) {
            return body;
        }
        // storeName이 있으면 "[storeName] 본문" 형식으로 반환
        return "[" + storeName + "] " + body;
    }

    /**
     * 아동용 메시지 반환
     */
    public String getChildMessageTemplate() {
        return childMessageTemplate;
    }

    /**
     * 아동용 메시지 반환 (storeName 포함)
     * storeName이 있으면 "[storeName] message", 없으면 "message"
     */
    public String getChildMessageTemplate(String storeName) {
        // childMessageTemplate이 없으면 null 반환
        if (childMessageTemplate == null) return null;
        // storeName이 없으면 가게 이름 메시지 제거
        if (storeName == null || storeName.isBlank()) return childMessageTemplate;
        return "[" + storeName + "] " + childMessageTemplate;
    }
}
