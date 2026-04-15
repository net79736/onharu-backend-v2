package com.backend.onharu.domain.store.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.store.dto.CategoryCacheDto;
import com.backend.onharu.domain.store.dto.CategoryQuery.FindAllByNameQuery;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.repository.CategoryRepository;
import com.backend.onharu.domain.support.CacheName;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryQueryService {

    private final CategoryRepository categoryRepository;

    /**
     * (캐시용) 카테고리 전체 목록 조회 - Redis 캐시 DTO로 반환합니다.
     *
     * <p>카테고리는 변경 빈도 대비 조회 빈도가 높아 캐시 효율이 좋습니다.</p>
     */
    @Cacheable(cacheNames = CacheName.STORE_CATEGORY_LIST, key = "'all'")
    public List<CategoryCacheDto> getAllCategoriesCache() {
        List<Category> categories = categoryRepository.findAllByName(new FindAllByNameQuery(null));
        return categories.stream().map(CategoryCacheDto::from).toList();
    }
}