package com.backend.onharu.domain.storeschedule.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdAndScheduleDateQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdAndYearMonthQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdAndScheduleDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdAndYearMonthParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.GetStoreScheduleByIdParam;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.repository.StoreScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreScheduleQueryService {
    private final StoreScheduleRepository storeScheduleRepository;

    /**
     * 가게 일정 단건 조회
     * 
     * @param query 가게 일정 ID
     * @return 조회된 가게 일정 엔티티
     */
    public StoreSchedule getStoreScheduleById(GetStoreScheduleByIdQuery query) {
        return storeScheduleRepository.getStoreScheduleById(
                new GetStoreScheduleByIdParam(query.storeScheduleId()));
    }

    /**
     * 가게 ID로 가게 일정 목록 조회
     * 
     * @param query 가게 ID
     * @return 가게 ID에 해당하는 가게 일정 리스트
     */
    public List<StoreSchedule> findAllByStoreId(FindAllByStoreIdQuery query) {
        return storeScheduleRepository.findAllByStoreId(
                new FindAllByStoreIdParam(query.storeId()));
    }

    /**
     * 가게 ID와 날짜로 가게 일정 목록 조회
     * 
     * @param query 가게 ID
     * @return 가게 ID에 해당하는 가게 일정 리스트
     */
    public List<StoreSchedule> findAllByStoreIdAndScheduleDate(FindAllByStoreIdAndScheduleDateQuery query) {
        return storeScheduleRepository.findAllByStoreIdAndScheduleDate(
                new FindAllByStoreIdAndScheduleDateParam(query.storeId(), query.scheduleDate()));
    }

    /**
     * 가게 ID와 연/월로 가게 일정 목록 조회
     * 
     * @param query 가게 ID, 연도, 월
     * @return 해당 연/월의 가게 일정 리스트
     */
    public List<StoreSchedule> findAllByStoreIdAndYearMonth(FindAllByStoreIdAndYearMonthQuery query) {
        return storeScheduleRepository.findAllByStoreIdAndYearMonth(
                new FindAllByStoreIdAndYearMonthParam(query.storeId(), query.year(), query.month()));
    }

    /**
     * 여러 가게 ID 중, 기준 날짜(포함) 이후 스케줄이 존재하는 가게 ID 집합을 반환합니다.
     * 목록 응답에서 isSharing 오버라이드 등 배치 판정에 사용합니다.
     */
    public Set<Long> findStoreIdsHavingScheduleOnOrAfter(Set<Long> storeIds, LocalDate today) {
        return storeScheduleRepository.findStoreIdsHavingScheduleOnOrAfter(storeIds, today);
    }
}
