package com.backend.onharu.application;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.backend.onharu.application.validator.StoreScheduleValidator;
import com.backend.onharu.domain.reservation.dto.ReservationQuery.FindByStoreIdQuery;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.reservation.service.ReservationQueryService;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdAndScheduleDateQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdAndYearMonthQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.policy.StoreScheduleDeletionPolicy;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.DateSummary;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.ScheduleSlot;

import lombok.RequiredArgsConstructor;

/**
 * 가게 일정 Facade
 */
@Component
@RequiredArgsConstructor
public class StoreScheduleFacade {

    private final StoreScheduleQueryService storeScheduleQueryService;
    private final ReservationQueryService reservationQueryService;
    private final StoreScheduleValidator storeScheduleValidator;

    /**
     * 월별 예약 가능 날짜 스케쥴 목록 조회
     * 
     * @param storeId 가게 ID
     * @param year    연도
     * @param month   월 (Optional)
     * @return 날짜별 예약 가능 슬롯 수를 포함한 스케줄 목록
     */
    public List<DateSummary> getMonthlySchedules(Long storeId, int year, int month) {
        // 1. 해당 가게의 [연도/월]에 해당하는 모든 스케줄 데이터를 DB에서 가져옵니다.
        List<StoreSchedule> allSchedules = storeScheduleQueryService.findAllByStoreIdAndYearMonth(
                new FindAllByStoreIdAndYearMonthQuery(storeId, year, month));

        // 2. 이미 예약이 완료된 스케줄 ID 목록을 가져옵니다. (WAITING, CONFIRMED, COMPLETED)
        Set<Long> reservedScheduleIds = getReservedScheduleIds(storeId);

        // 3. [날짜별]로 "예약 가능한" 슬롯이 몇 개인지 미리 계산해서 Map에 저장
        // 과거 날짜 슬롯 및 이미 예약된 슬롯은 제외
        Map<LocalDate, Long> availableSlotsByDate = allSchedules.stream()
                .filter(schedule -> storeScheduleValidator.isAvailable(schedule, reservedScheduleIds))
                .collect(Collectors.groupingBy(StoreSchedule::getScheduleDate, Collectors.counting()));

        // 스케줄이 있는 날짜 전체를 기준으로 summary 생성 (예약 불가 날짜도 0으로 포함)
        return allSchedules.stream()
                .map(StoreSchedule::getScheduleDate) // 스케줄 객체에서 날짜만 추출
                .distinct()                          // 중복 날짜 제거
                .sorted()                            // 날짜 순서대로 정렬
                .map(date -> new DateSummary(
                        date,
                        // 이 날짜에 예약 가능한 슬롯 수
                        availableSlotsByDate.getOrDefault(date, 0L).intValue(),
                        // 이 날짜의 모든 시간대 슬롯 목록
                        allSchedules.stream()
                                .filter(schedule -> schedule.getScheduleDate().equals(date))
                                .map(schedule -> new ScheduleSlot(
                                        schedule,
                                        storeScheduleValidator.isAvailable(schedule, reservedScheduleIds)
                                ))
                                .toList()
                ))
                .toList();
    }

    /**
     * 특정 날짜의 시간대별 스케줄 상세 조회 (year+month+day 조회 시)
     *
     * 해당 날짜의 모든 스케줄과 각 슬롯의 예약 가능 여부를 함께 반환합니다.
     *
     * @param storeId      가게 ID
     * @param scheduleDate 조회할 날짜
     * @return 시간대별 스케줄 상세 목록 (예약 가능 여부 포함)
     */
    public List<ScheduleSlot> getDailyScheduleDetails(Long storeId, LocalDate scheduleDate) {
        List<StoreSchedule> allSchedules = storeScheduleQueryService.findAllByStoreIdAndScheduleDate(
                new FindAllByStoreIdAndScheduleDateQuery(storeId, scheduleDate));

        // 예약 가능 여부 확인을 위한 활성 예약(WAITING, CONFIRMED, COMPLETED) ID 목록 조회
        Set<Long> reservedScheduleIds = getReservedScheduleIds(storeId);

        return allSchedules.stream()
                .map(schedule -> new ScheduleSlot(
                        schedule,
                        storeScheduleValidator.isAvailable(schedule, reservedScheduleIds)
                ))
                .toList();
    }

    /**
     * 여러 가게 ID 중, "오늘(포함) 이후" 스케줄이 존재하는 가게 ID 집합을 반환합니다.
     * (정의 A: scheduleDate >= today)
     */
    public Set<Long> filterReservableStoreIds(Set<Long> storeIds, LocalDate today) {
        return storeScheduleQueryService.findStoreIdsHavingScheduleOnOrAfter(storeIds, today);
    }

    /**
     * 해당 가게의 활성 예약(WAITING, CONFIRMED, COMPLETED) 스케줄 ID Set을 반환합니다.
     * CANCELED 상태는 예약 가능한 것으로 간주합니다.
     */
    private Set<Long> getReservedScheduleIds(Long storeId) {
        List<Reservation> reservations = reservationQueryService.findByStoreId(
                new FindByStoreIdQuery(storeId));

        return reservations.stream()
                .filter(reservation -> StoreScheduleDeletionPolicy.isBlockingStatus(reservation.getStatus()))
                .map(reservation -> reservation.getStoreSchedule().getId())
                .collect(Collectors.toSet());
    }
}
