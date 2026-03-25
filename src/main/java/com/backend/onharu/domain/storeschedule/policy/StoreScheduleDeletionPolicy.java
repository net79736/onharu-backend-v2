package com.backend.onharu.domain.storeschedule.policy;

import java.util.Set;

import com.backend.onharu.domain.common.enums.ReservationType;

/**
 * 가게 일정(슬롯) 삭제 시점에 "삭제를 막아야 하는 예약 상태"를 판단하는 정책입니다.
 * <p>
 * 취소({@link ReservationType#CANCELED})는 일정 삭제에 대해 예외적으로 허용합니다.
 * 그 외 활성 예약(WAITING, CONFIRMED, COMPLETED)이 존재하면 일정 삭제를 제한합니다.
 */
public final class StoreScheduleDeletionPolicy {

    private StoreScheduleDeletionPolicy() {
    }

    /**
     * 일정 삭제를 막는 예약 상태 집합
     */
    public static final Set<ReservationType> BLOCKING_STATUSES = Set.of(
            ReservationType.WAITING,
            ReservationType.CONFIRMED,
            ReservationType.COMPLETED
    );

    public static boolean isBlockingStatus(ReservationType status) {
        return BLOCKING_STATUSES.contains(status);
    }
}

