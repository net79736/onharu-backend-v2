package com.backend.onharu.domain.store.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.backend.onharu.domain.store.dto.StoreQuery.FindByCategoryIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByNameQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.SearchStoresQuery;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByCategoryIdParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByNameParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.FindByOwnerIdParam;
import com.backend.onharu.domain.store.dto.StoreRepositroyParam.GetStoreByIdParam;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StoreQueryService {
    private final StoreRepository storeRepository;

    /**
     * 가게 단건 조회
     * 
     * @param query 가게 ID
     * @return 조회된 Store 엔티티 (없으면 예외 발생)
     */
    public Store getStore(GetStoreByIdQuery query) {
        return storeRepository.getStore(new GetStoreByIdParam(query.storeId()));
    }

    /**
     * 사업자 ID로 가게 목록 조회
     * 
     * @param query 사업자 ID
     * @return 사업자 ID에 해당하는 가게 리스트
     */
    public List<Store> findByOwnerId(FindByOwnerIdQuery query) {
        return storeRepository.findByOwnerId(
                new FindByOwnerIdParam(query.ownerId()));
    }

    /**
     * 카테고리 ID로 가게 목록 조회
     * 
     * @param query 카테고리 ID
     * @return 카테고리 ID에 해당하는 가게 리스트
     */
    public List<Store> findByCategoryId(FindByCategoryIdQuery query) {
        return storeRepository.findByCategoryId(
                new FindByCategoryIdParam(query.categoryId()));
    }

    /**
     * 가게 이름으로 검색
     * 
     * @param query 가게 이름
     * @return 이름이 포함된 가게 리스트
     */
    public List<Store> findByName(FindByNameQuery query) {
        return storeRepository.findByName(
                new FindByNameParam(query.name()));
    }

    /**
     * 가게 목록 조회 (위치 기반 검색)
     * 
     * 위치 정보(latitude, longitude, radius)가 모두 제공되면 위치 기반 검색을 수행하고,
     * 하나라도 없으면 전체 가게 목록을 반환합니다.
     * 
     * @param searchStoresQuery 위치 기반 검색 쿼리
     * @param pageable 페이징 정보
     * @return 가게 목록
     */
    public Page<Store> findByLocation(SearchStoresQuery searchStoresQuery, Pageable pageable) {
        // 위치 정보가 모두 제공되지 않으면 전체 조회
        if (searchStoresQuery.latitude() == null 
                || searchStoresQuery.longitude() == null 
                || searchStoresQuery.radius() == null) {
            return storeRepository.findAllWithCategory(pageable);
        }
        
        // TODO: 위치 기반 검색 구현 (Haversine 공식 사용하여 반경 내 가게 필터링)
        // 현재는 위치 정보가 있어도 전체 조회 (추후 구현 예정)
        return storeRepository.findAllWithCategory(pageable);
    }
}
