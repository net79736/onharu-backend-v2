package com.backend.onharu.domain.store.repository;

import java.util.List;

import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByCategoryIdParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByNameParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByOwnerIdParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.GetStoreByIdParam;
import com.backend.onharu.domain.store.model.Store;

/**
 * 가게 Repository 인터페이스
 */
public interface StoreRepository {

    /**
     * 가게 저장 및 수정
     */
    Store save(Store store);

    /**
     * 가게 삭제
     */
    void delete(Store store);

    /**
     * 가게 단건 조회
     */
    Store getStore(GetStoreByIdParam param);

    /**
     * 가게 목록 조회
     */
    List<Store> findAll();
    
    /**
     * 사업자 ID로 가게 목록 조회
     */
    List<Store> findByOwnerId(FindByOwnerIdParam param);

    /**
     * 카테고리 ID로 가게 목록 조회
     */
    List<Store> findByCategoryId(FindByCategoryIdParam param);

    /**
     * 가게 이름으로 검색
     */
    List<Store> findByName(FindByNameParam param);
}
