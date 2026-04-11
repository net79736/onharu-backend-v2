package com.backend.onharu.infra.db.store.impl;

import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.store.model.BusinessHours;
import com.backend.onharu.domain.store.repository.BusinessHoursRepository;
import com.backend.onharu.infra.db.store.BusinessHoursJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class BusinessHoursRepositoryImpl implements BusinessHoursRepository {
    private final BusinessHoursJpaRepository businessHoursJpaRepository;

    @Override
    public List<BusinessHours> findAllByStoreIds(List<Long> storeIds) {
        // null/empty 체크
        if (storeIds == null || storeIds.isEmpty()) {
            return List.of();
        }

        // 중복 제거
        List<Long> normalized = storeIds.stream()
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        // null/empty 체크
        if (normalized.isEmpty()) {
            return List.of();
        }

        return businessHoursJpaRepository.findAllByStoreIds(normalized);
    }
}