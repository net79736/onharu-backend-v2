package com.backend.onharu.domain.level.dto;

import com.backend.onharu.domain.level.model.Level;

/**
 * Redis Hash 캐시용 Level DTO.
 *
 * <p>JPA 연관관계(LAZY 컬렉션 등)를 제거하고 필요한 필드만 평탄화합니다.</p>
 */
public record LevelCacheDto(
        Long id,
        String name,
        int conditionNumber
) {
    public static LevelCacheDto from(Level level) {
        return new LevelCacheDto(level.getId(), level.getName(), level.getConditionNumber());
    }
}