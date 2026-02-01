package com.backend.onharu.domain.storeschedule.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByBusinessDayQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindAllByStoreIdQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindByStoreIdAndBusinessDayQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.FindByStoreIdAndDateQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleQuery.GetStoreScheduleByIdQuery;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByScheduleDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindByStoreIdAndDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindByStoreIdAndScheduleDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.GetStoreScheduleByIdParam;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.repository.StoreScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
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
     * 영업일로 가게 일정 목록 조회
     * 
     * @param query 영업일
     * @return 영업일이 일치하는 가게 일정 리스트
     */
    public List<StoreSchedule> findAllByBusinessDay(FindAllByBusinessDayQuery query) {
        return storeScheduleRepository.findAllByBusinessDay(
                new FindAllByScheduleDateParam(query.scheduleDate()));
    }

    /**
     * 가게 ID와 영업일로 가게 일정 조회
     * 
     * @param query 가게 ID와 영업일
     * @return 가게 ID와 영업일이 일치하는 가게 일정
     */
    public List<StoreSchedule> findByStoreIdAndBusinessDay(FindByStoreIdAndBusinessDayQuery query) {
        return storeScheduleRepository.findByStoreIdAndScheduleDate(
                new FindByStoreIdAndScheduleDateParam(query.storeId(), query.scheduleDate()));
    }

    /**
     * 특정 날짜에 해당하는 요일의 가게 일정 조회
     * 
     * @param query 가게 ID와 날짜
     * @return 날짜에 해당하는 요일의 가게 일정 리스트
     */
    public List<StoreSchedule> findByStoreIdAndDate(FindByStoreIdAndDateQuery query) {
        return storeScheduleRepository.findByStoreIdAndDate(
                new FindByStoreIdAndDateParam(query.storeId(), query.scheduleDate()));
    }
}
