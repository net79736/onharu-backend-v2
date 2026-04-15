package com.backend.onharu.domain.store.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.backend.onharu.domain.store.dto.StoreQuery.FindWithCategoryAndFavoriteCountByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindAllWithCategoryAndFavoriteCountParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByCategoryIdParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByNameParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByOwnerIdParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindWithCategoryAndFavoriteCountByLocationParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.GetStoreByIdParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.GetStoreDetailByIdAndLocationParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.GetStoreDetailByIdParam;
import com.backend.onharu.domain.store.dto.StoreWithFavoriteCount;
import com.backend.onharu.domain.store.dto.StoreWithFavoriteCountByLocationProjection;
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
    Store getStoreById(GetStoreByIdParam param);

    /**
     * 가게 상세 정보 조회
     */
    StoreWithFavoriteCount getStoreDetailById(GetStoreDetailByIdParam param);

    /**
     * 가게 상세 정보 조회 (위치 기반)
     */
    StoreWithFavoriteCountByLocationProjection getStoreDetailByIdAndLocation(GetStoreDetailByIdAndLocationParam param);

    /**
     * 거리만 조회 (가게 상세와 분리)
     */
    Double getStoreDistanceByIdAndLocation(GetStoreDetailByIdAndLocationParam param);

    /**
     * ID 목록으로 가게 목록 조회
     */
    List<Store> findByIds(List<Long> ids);

    /**
     * 페이징된 가게 목록 조회 (찜 개수 포함, 찜 많은 순)
     */
    Page<StoreWithFavoriteCount> findWithCategoryAndFavoriteCountByOwnerId(FindWithCategoryAndFavoriteCountByOwnerIdQuery param, Pageable pageable);

    /**
     * 페이징된 가게 목록 조회 (찜 개수 포함, 찜 많은 순)
     */
    Page<StoreWithFavoriteCount> findAllWithCategoryAndFavoriteCount(FindAllWithCategoryAndFavoriteCountParam param, Pageable pageable);

    /**
     * 위치 기반 페이징된 가게 목록 조회
     */
    Page<StoreWithFavoriteCountByLocationProjection> findWithCategoryAndFavoriteCountByLocationProjection(FindWithCategoryAndFavoriteCountByLocationParam param, Pageable pageable);

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
