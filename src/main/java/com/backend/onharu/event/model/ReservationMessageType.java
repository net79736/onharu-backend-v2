package com.backend.onharu.event.model;

/**
 * 예약 이벤트 메시지의 envelope type을 한곳에서 관리합니다.
 * <p>
 * transport(RabbitMQ/Kafka/HTTP 등)가 바뀌어도 메시지 계약은 유지되도록, 인프라가 아닌 이벤트 모델 계층에 둡니다.
 */
public enum ReservationMessageType {

    RESERVATION_NOTIFICATION("RESERVATION_NOTIFICATION"); // 예약 알림 메시지

    private final String value;

    ReservationMessageType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }
}