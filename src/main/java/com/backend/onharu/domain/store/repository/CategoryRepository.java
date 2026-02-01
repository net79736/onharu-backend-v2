package com.backend.onharu.domain.store.repository;

import java.util.List;

import com.backend.onharu.domain.store.dto.CategoryQuery.FindAllByNameQuery;
import com.backend.onharu.domain.store.dto.CategoryQuery.GetCategoryByIdQuery;
import com.backend.onharu.domain.store.model.Category;

/**
 * 카테고리 Repository 인터페이스
 */
public interface CategoryRepository {
    /**
     * 카테고리 저장 및 수정
     */
    Category save(Category category);

    /**
     * 카테고리 삭제
     */
    void delete(Category category);

    /**
     * 카테고리 단건 조회
     */
    Category getCategoryById(GetCategoryByIdQuery query);

    /**
     * 카테고리 이름으로 검색
     */
    List<Category> findAllByName(FindAllByNameQuery query);
}
