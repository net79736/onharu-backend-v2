package com.backend.onharu.infra.db.storeschedule.impl;

import static com.backend.onharu.domain.support.error.ErrorType.StoreSchedule.STORE_SCHEDULE_NOT_FOUND;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdAndScheduleDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdAndYearMonthParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindByStoreIdAndTimeRangeOverlapParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.GetStoreScheduleByIdParam;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.repository.StoreScheduleRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 가게 일정 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class StoreScheduleRepositoryImpl implements StoreScheduleRepository {

    private final StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Override
    public StoreSchedule save(StoreSchedule storeSchedule) {
        return storeScheduleJpaRepository.save(storeSchedule);
    }

    @Override
    public List<StoreSchedule> saveAll(List<StoreSchedule> storeSchedules) {
        return storeScheduleJpaRepository.saveAll(storeSchedules);
    }

    @Override
    public StoreSchedule getStoreScheduleById(GetStoreScheduleByIdParam param) {
        return storeScheduleJpaRepository.findById(param.storeScheduleId())
                .orElseThrow(() -> new CoreException(STORE_SCHEDULE_NOT_FOUND));
    }

    @Override
    public List<StoreSchedule> findAllByStoreId(FindAllByStoreIdParam param) {
        return storeScheduleJpaRepository.findByStoreId(param.storeId());
    }

    @Override
    public List<StoreSchedule> findAllByStoreIdAndScheduleDate(FindAllByStoreIdAndScheduleDateParam param) {
        return storeScheduleJpaRepository.findAllByStoreIdAndScheduleDate(param.storeId(), param.scheduleDate());
    }

    @Override
    public List<StoreSchedule> findByStoreIdAndTimeRangeOverlap(FindByStoreIdAndTimeRangeOverlapParam param) {
        List<StoreSchedule> schedules;
        
        if (param.storeId() != null) {
            schedules = storeScheduleJpaRepository.findByStoreId(param.storeId());
        } else {
            schedules = storeScheduleJpaRepository.findAll();
        }
        
        // 시간 범위가 겹치는 일정만 필터링
        return schedules.stream()
                .filter(schedule -> isTimeRangeOverlap(
                        param.startTime(),
                        param.endTime(),
                        schedule.getStartTime(),
                        schedule.getEndTime()))
                .toList();
    }

    @Override
    public List<StoreSchedule> findAllByStoreIdAndYearMonth(FindAllByStoreIdAndYearMonthParam param) {
        LocalDate startDate = LocalDate.of(param.year(), param.month(), 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());
        return storeScheduleJpaRepository.findAllByStoreIdAndScheduleDateBetween(param.storeId(), startDate, endDate);
    }

    @Override
    public void delete(StoreSchedule storeSchedule) {
        storeScheduleJpaRepository.delete(storeSchedule);
    }

    /**
     * 시간 범위가 겹치는지 확인
     * 요청한 시간 범위(startTime ~ endTime)와 일정의 영업시간(openTime ~ closeTime)이 겹치는지 확인
     */
    private boolean isTimeRangeOverlap(LocalTime startTime, LocalTime endTime,
                                      LocalTime openTime, LocalTime closeTime) {
        // 요청한 시간 범위가 영업시간과 겹치는 경우
        // 겹치는 조건: startTime < closeTime && endTime > openTime
        return startTime.isBefore(closeTime) && endTime.isAfter(openTime);
    }
}
