package com.backend.onharu.domain.reservation.support;

/**
 * Reservation 목록 조회 시 정렬 필드를 JPA Property 경로로 변환합니다.
 */
public final class ReservationSearchSortResolver {

    private ReservationSearchSortResolver() {
    }

    /**
     * API 요청의 정렬 필드를 실제 JPA 정렬에 사용할 필드 경로로 변환합니다.
     *
     * @param requestedSortField 요청된 정렬 필드 (id, scheduleDate, reservationAt)
     * @return 변환된 정렬 필드 경로
     */
    public static String resolve(String requestedSortField) {
        if (requestedSortField == null || requestedSortField.isBlank()) {
            return "id";
        }

        return switch (requestedSortField) {
            case "scheduleDate" -> "storeSchedule.scheduleDate";
            case "reservationAt" -> "reservationAt";
            case "id" -> "id";
            default -> "id";
        };
    }
}

