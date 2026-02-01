package com.backend.onharu.infra.db.store;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.store.model.Category;

/**
 * 카테고리 JPA Repository
 */
public interface CategoryJpaRepository extends JpaRepository<Category, Long> {

    /**
     * 카테고리 이름으로 검색
     */
    List<Category> findAllByNameContainingOrderByNameAsc(String name);

    /**
     * 모든 카테고리를 이름 순으로 조회
     */
    List<Category> findAllByOrderByNameAsc();
}
