package com.backend.onharu.infra.db.store;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.backend.onharu.domain.store.model.Store;

/**
 * 가게 JPA Repository
 */
public interface StoreJpaRepository extends JpaRepository<Store, Long> {
    
    /**
     * 페이징된 가게 목록 조회
     */
    @Query("SELECT s FROM Store s LEFT JOIN FETCH s.category")
    Page<Store> findAllWithCategory(Pageable pageable);
    
    /**
     * 사업자 ID로 가게 목록 조회
     */
    List<Store> findByOwner_Id(Long ownerId);

    /**
     * 카테고리 ID로 가게 목록 조회
     */
    List<Store> findByCategory_Id(Long categoryId);

    /**
     * 가게 이름으로 검색
     */
    List<Store> findAllByNameContainingIgnoreCase(String name);
}
