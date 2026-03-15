package com.backend.onharu.application.validator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdAndScheduleDateQuery;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.service.StoreScheduleQueryService;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

/**
 * 가게 일정 중복 검증을 담당하는 Validator
 * 
 * 요청된 일정들 간의 중복과 기존 DB 일정과의 중복을 검증합니다.
 */
@Component
@RequiredArgsConstructor
public class StoreScheduleValidator {

    private final StoreScheduleQueryService storeScheduleQueryService;

    /**
     * 일정 시간 범위 정보를 담는 공통 DTO
     * 생성/수정 요청 모두를 처리할 수 있도록 공통 인터페이스 역할을 합니다.
     */
    public record ScheduleTimeRange(
            LocalDate scheduleDate,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }

    /**
     * 일정 중복 검증을 수행합니다.
     * 
     * 1. 요청된 일정들 간의 중복 체크
     * 2. 기존 DB 일정과의 중복 체크
     * 
     * @param storeId 가게 ID
     * @param timeRanges 검증할 일정 시간 범위 목록
     * @param excludeScheduleIds 제외할 일정 ID 목록 (수정 시 자기 자신 제외용, null이면 제외 없음)
     * @throws CoreException 중복된 일정이 있는 경우
     */
    public void validateNoDuplicates(
            Long storeId,
            List<ScheduleTimeRange> timeRanges,
            Set<Long> excludeScheduleIds) {
        
        // 1. 요청 내부 중복 체크
        validateInternalDuplicates(timeRanges);
        
        // 2. DB와의 중복 체크
        validateExternalDuplicates(storeId, timeRanges, excludeScheduleIds);
    }

    /**
     * 요청된 일정들 간의 중복 체크
     * 같은 날짜에 시간 범위가 겹치는 일정이 있는지 확인합니다.
     * 
     * 날짜별로 그룹화하여 O(n²) 검증을 수행하지만, 같은 날짜의 일정만 비교하므로 효율적입니다.
     * 
     * @param timeRanges 검증할 일정 시간 범위 목록
     * @throws CoreException 중복된 일정이 있는 경우
     */
    private void validateInternalDuplicates(List<ScheduleTimeRange> timeRanges) {
        // 날짜별로 그룹화
        Map<LocalDate, List<ScheduleTimeRange>> groupedByDate = timeRanges.stream()
                .collect(Collectors.groupingBy(ScheduleTimeRange::scheduleDate));

        // 각 날짜별로 시간 겹침 체크
        groupedByDate.forEach((date, dailySchedules) -> {
            checkTimeOverlaps(dailySchedules);
        });
    }

    /**
     * 같은 날짜의 일정들 간 시간 겹침 체크
     * 
     * @param schedules 같은 날짜의 일정 목록
     * @throws CoreException 시간 범위가 겹치는 경우
     */
    private void checkTimeOverlaps(List<ScheduleTimeRange> schedules) {
        for (int i = 0; i < schedules.size(); i++) {
            ScheduleTimeRange schedule1 = schedules.get(i);
            for (int j = i + 1; j < schedules.size(); j++) {
                ScheduleTimeRange schedule2 = schedules.get(j);
                
                if (isTimeRangeOverlap(
                        schedule1.startTime(), schedule1.endTime(),
                        schedule2.startTime(), schedule2.endTime())) {
                    throw new CoreException(ErrorType.StoreSchedule.STORE_SCHEDULE_DUPLICATE);
                }
            }
        }
    }

    /**
     * 기존 DB 일정과의 중복 체크
     * 같은 가게, 같은 날짜에 시간 범위가 겹치는 기존 일정이 있는지 확인합니다.
     * 
     * @param storeId 가게 ID
     * @param timeRanges 검증할 일정 시간 범위 목록
     * @param excludeScheduleIds 제외할 일정 ID 목록 (수정 시 자기 자신 제외용, null이면 제외 없음)
     * @throws CoreException 중복된 일정이 있는 경우
     */
    private void validateExternalDuplicates(
            Long storeId,
            List<ScheduleTimeRange> timeRanges,
            Set<Long> excludeScheduleIds) {
        
        for (ScheduleTimeRange timeRange : timeRanges) {
            // 같은 날짜의 기존 일정 조회
            List<StoreSchedule> existingSchedules = storeScheduleQueryService.findAllByStoreIdAndScheduleDate(
                    new FindAllByStoreIdAndScheduleDateQuery(storeId, timeRange.scheduleDate()));

            // 기존 일정과 시간 범위가 겹치는지 확인
            existingSchedules.stream()
                    .filter(existing -> excludeScheduleIds == null || !excludeScheduleIds.contains(existing.getId()))
                    .filter(existing -> isTimeRangeOverlap(
                            timeRange.startTime(), timeRange.endTime(),
                            existing.getStartTime(), existing.getEndTime()))
                    .findFirst()
                    .ifPresent(duplicate -> {
                        throw new CoreException(ErrorType.StoreSchedule.STORE_SCHEDULE_DUPLICATE);
                    });
        }
    }

    /**
     * 시간 범위가 겹치는지 확인
     * 두 시간 범위가 겹치는지 확인합니다.
     * 
     * 겹치는 조건: startTime1 < endTime2 && endTime1 > startTime2
     * 
     * @param startTime1 첫 번째 시간 범위의 시작 시간
     * @param endTime1 첫 번째 시간 범위의 종료 시간
     * @param startTime2 두 번째 시간 범위의 시작 시간
     * @param endTime2 두 번째 시간 범위의 종료 시간
     * @return 겹치면 true, 겹치지 않으면 false
     */
    private boolean isTimeRangeOverlap(
            LocalTime startTime1, LocalTime endTime1,
            LocalTime startTime2, LocalTime endTime2) {
        // 시간 범위가 겹치는 조건: startTime1 < endTime2 && endTime1 > startTime2
        return startTime1.isBefore(endTime2) && endTime1.isAfter(startTime2);
    }
}
