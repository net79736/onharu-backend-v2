package com.backend.onharu.domain.storeschedule.repository;

import java.util.List;

import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByScheduleDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdAndScheduleDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindAllByStoreIdParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindByStoreIdAndDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindByStoreIdAndScheduleDateParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.FindByStoreIdAndTimeRangeOverlapParam;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.GetStoreScheduleByIdParam;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;

public interface StoreScheduleRepository {

    /**
     * 가게 일정 저장 및 수정
     */
    StoreSchedule save(StoreSchedule storeSchedule);

    /**
     * 가게 일정 일괄 저장
     */
    List<StoreSchedule> saveAll(List<StoreSchedule> storeSchedules);

    /**
     * 가게 일정 삭제
     */
    void delete(StoreSchedule storeSchedule);

    /**
     * 가게 일정 단건 조회
     */
    StoreSchedule getStoreScheduleById(GetStoreScheduleByIdParam param);

    /**
     * 가게 ID로 가게 일정 목록 조회
     */
    List<StoreSchedule> findAllByStoreId(FindAllByStoreIdParam param);

    /**
     * 가게 ID와 날짜로 가게 일정 목록 조회
     */
    List<StoreSchedule> findAllByStoreIdAndScheduleDate(FindAllByStoreIdAndScheduleDateParam param);

    /**
     * 영업일로 가게 일정 목록 조회
     */
    List<StoreSchedule> findAllByScheduleDate(FindAllByScheduleDateParam param);

    /**
     * 가게 ID와 영업일로 가게 일정 조회
     */
    List<StoreSchedule> findByStoreIdAndScheduleDate(FindByStoreIdAndScheduleDateParam param);

    /**
     * 특정 날짜에 해당하는 가게 일정 조회
     */
    List<StoreSchedule> findByStoreIdAndDate(FindByStoreIdAndDateParam param);

    /**
     * 특정 시간 범위와 겹치는 가게 일정 조회
     */
    List<StoreSchedule> findByStoreIdAndTimeRangeOverlap(FindByStoreIdAndTimeRangeOverlapParam param);
}
