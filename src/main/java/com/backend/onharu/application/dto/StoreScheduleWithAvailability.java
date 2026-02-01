package com.backend.onharu.application.dto;

import java.util.List;
import java.util.Set;

import com.backend.onharu.domain.storeschedule.model.StoreSchedule;

import lombok.Builder;

/**
 * 예약 가능 여부가 포함된 가게 일정 정보
 * 
 * Facade에서 모든 일정과 예약 가능한 일정 ID Set을 함께 반환하기 위한 DTO
 */
@Builder
public record StoreScheduleWithAvailability(
        /**
         * 가게의 모든 일정 목록
         */
        List<StoreSchedule> allSchedules,
        
        /**
         * 예약 가능한 일정 ID Set
         */
        Set<Long> availableScheduleIds
) {
}
