package com.backend.onharu.infra.db.store.impl;

import static com.backend.onharu.domain.support.error.ErrorType.Category.CATEGORY_NOT_FOUND;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.store.dto.CategoryQuery.FindAllByNameQuery;
import com.backend.onharu.domain.store.dto.CategoryQuery.GetCategoryByIdQuery;
import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.repository.CategoryRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 카테고리 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class CategoryRepositoryImpl implements CategoryRepository {
    
    private final CategoryJpaRepository categoryJpaRepository;

    @Override
    public Category save(Category category) {
        return categoryJpaRepository.save(category);
    }

    @Override
    public void delete(Category category) {
        categoryJpaRepository.delete(category);
    }

    @Override
    public Category getCategoryById(GetCategoryByIdQuery query) {
        return categoryJpaRepository.findById(query.categoryId())
                .orElseThrow(() -> new CoreException(CATEGORY_NOT_FOUND));
    }

    @Override
    public List<Category> findAllByName(FindAllByNameQuery query) {
        // name이 null이거나 비어있으면 전체 목록 조회
        if (query.name() == null || query.name().isBlank()) {
            return categoryJpaRepository.findAllByOrderByNameAsc();
        }
        // name이 있으면 이름으로 필터링하여 조회
        return categoryJpaRepository.findAllByNameContainingOrderByNameAsc(query.name());
    }
}
