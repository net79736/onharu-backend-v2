package com.backend.onharu.domain.store.dto;

import com.backend.onharu.domain.store.model.Category;

/**
 * Redis 캐시용 카테고리 DTO.
 *
 * <p>JPA 엔티티를 캐시에 직접 넣지 않고, 필요한 필드만 평탄화합니다.</p>
 */
public record CategoryCacheDto(
        Long id,
        String name
) {
    public static CategoryCacheDto from(Category category) {
        return new CategoryCacheDto(category.getId(), category.getName());
    }
}

