package com.backend.onharu.domain.store.repository;

import java.util.List;

import com.backend.onharu.domain.store.model.BusinessHours;

/**
 * 가게 영업시간 조회를 위한 도메인 Repository 포트.
 */
public interface BusinessHoursRepository {
    /**
     * storeIds에 해당하는 영업시간 목록을 반환합니다.
     * storeIds가 null/empty이면 빈 리스트를 반환합니다.
     */
    List<BusinessHours> findAllByStoreIds(List<Long> storeIds);
}

