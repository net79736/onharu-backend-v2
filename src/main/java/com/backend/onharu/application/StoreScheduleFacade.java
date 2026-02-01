package com.backend.onharu.application;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.backend.onharu.application.dto.StoreScheduleWithAvailability;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByStoreIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;

import lombok.RequiredArgsConstructor;

/**
 * 가게 일정 Facade
 */
@Component
@RequiredArgsConstructor
public class StoreScheduleFacade {

    private final StoreScheduleQueryService storeScheduleQueryService;
    private final ReservationQueryService reservationQueryService;

    /**
     * 가게의 모든 일정 조회
     * 
     * @param storeId 가게 ID
     * @return 가게의 모든 일정 목록
     */
    public List<StoreSchedule> getAllStoreSchedules(Long storeId) {
        return storeScheduleQueryService.findAllByStoreId(new FindAllByStoreIdQuery(storeId));
    }

    /**
     * 가게의 예약 가능한 날짜 조회
     * 
     * @param storeId 가게 ID
     * @return 예약 가능한 일정 목록
     */
    public List<StoreSchedule> getAvailableDates(Long storeId) {
        // 1. 가게의 모든 일정 조회
        List<StoreSchedule> allSchedules = storeScheduleQueryService.findAllByStoreId(
                new FindAllByStoreIdQuery(storeId));

        // 2. 해당 가게의 모든 예약 조회
        List<Reservation> reservations = reservationQueryService.findByStoreId(
                new FindByStoreIdQuery(storeId));

        // 3. 활성 예약(WAITING, CONFIRMED, COMPLETED)이 있는 일정 ID Set 생성
        // CANCELED 상태는 예약 가능한 것으로 간주
        Set<Long> reservedScheduleIds = reservations.stream()
                .filter(reservation -> {
                    ReservationType status = reservation.getStatus();
                    return status == ReservationType.WAITING 
                            || status == ReservationType.CONFIRMED 
                            || status == ReservationType.COMPLETED;
                })
                .map(reservation -> reservation.getStoreSchedule().getId())
                .collect(Collectors.toSet());

        // 4. 예약되지 않은 일정만 필터링하여 반환
        return allSchedules.stream()
                .filter(schedule -> !reservedScheduleIds.contains(schedule.getId()))
                .collect(Collectors.toList());
    }

    /**
     * 가게의 모든 일정과 예약 가능 여부를 함께 조회
     * 
     * 모든 일정을 조회하고, 각 일정의 예약 가능 여부를 계산하여 함께 반환합니다.
     * 
     * @param storeId 가게 ID
     * @return 모든 일정 목록과 예약 가능한 일정 ID Set을 포함한 DTO
     */
    public StoreScheduleWithAvailability getAllStoreSchedulesWithAvailability(Long storeId) {
        // 1. 가게의 모든 일정 조회
        List<StoreSchedule> allSchedules = storeScheduleQueryService.findAllByStoreId(
                new FindAllByStoreIdQuery(storeId));

        // 2. 해당 가게의 모든 예약 조회
        List<Reservation> reservations = reservationQueryService.findByStoreId(
                new FindByStoreIdQuery(storeId));

        // 3. 활성 예약(WAITING, CONFIRMED, COMPLETED)이 있는 일정 ID Set 생성
        // CANCELED 상태는 예약 가능한 것으로 간주
        Set<Long> reservedScheduleIds = reservations.stream()
                .filter(reservation -> {
                    ReservationType status = reservation.getStatus();
                    return status == ReservationType.WAITING 
                            || status == ReservationType.CONFIRMED 
                            || status == ReservationType.COMPLETED;
                })
                .map(reservation -> reservation.getStoreSchedule().getId())
                .collect(Collectors.toSet());

        // 4. 예약 가능한 일정 ID Set 계산 (모든 일정 ID에서 예약된 일정 ID 제외)
        Set<Long> availableScheduleIds = allSchedules.stream()
                .map(StoreSchedule::getId)
                .filter(id -> !reservedScheduleIds.contains(id))
                .collect(Collectors.toSet());

        return StoreScheduleWithAvailability.builder()
                .allSchedules(allSchedules)
                .availableScheduleIds(availableScheduleIds)
                .build();
    }
    
}
