package com.backend.onharu.domain.store.service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.store.dto.StoreCacheDto;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByCategoryIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByNameQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.FindWithCategoryAndFavoriteCountByOwnerIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreByIdQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.GetStoreQuery;
import com.backend.onharu.domain.store.dto.StoreQuery.SearchStoresQuery;
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
import com.backend.onharu.domain.store.repository.StoreRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StoreQueryService {
    private final StoreRepository storeRepository;

    /**
     * 가게 단건 조회
     *
     * @param query 가게 ID
     * @return 조회된 Store 엔티티 (없으면 예외 발생)
     */
    public Store getStoreById(GetStoreByIdQuery query) {
        return storeRepository.getStoreById(new GetStoreByIdParam(query.storeId()));
    }

    /**
     * 가게 상세 정보 조회
     *
     * @param query 가게 ID
     * @return 조회된 가게 상세 정보
     * @return
     */
    public StoreWithFavoriteCount getStoreDetailById(GetStoreByIdQuery query) {
        return storeRepository.getStoreDetailById(new GetStoreDetailByIdParam(query.storeId()));
    }

    /**
     * (캐시용) 가게 상세 정보 조회 - Redis 캐시 DTO로 반환합니다.
     * <p>
     * 엔티티 그래프를 캐시에 넣지 않기 위해 {@link StoreCacheDto}로 평탄화합니다.
     */
    @Cacheable(cacheNames = "storeDetail", key = "'storeId:' + #query.storeId()")
    public StoreCacheDto getStoreDetailCacheById(GetStoreByIdQuery query) {
        return StoreCacheDto.from(getStoreDetailById(query));
    }

    /**
     * 가게 상세 정보 조회
     *
     * @param query 가게 ID
     * @return 조회된 가게 상세 정보
     * @return
     */
    @Deprecated(forRemoval = true)
    public StoreWithFavoriteCount getStoreDetailByIdAndLocation(GetStoreQuery query) {
        StoreWithFavoriteCountByLocationProjection content = storeRepository.getStoreDetailByIdAndLocation(new GetStoreDetailByIdAndLocationParam(query.storeId(), query.lat(), query.lng()));
        // 가게 엔티티 조회
        Store store = storeRepository.getStoreById(new GetStoreByIdParam(content.getId()));
        return new StoreWithFavoriteCount(store, content.getDistance(), content.getFavoriteCount());
    }

    /**
     * 거리만 조회 (가게 상세와 분리)
     *
     * <p>lat/lng가 있을 때 distance는 항상 DB에서만 계산합니다.</p>
     */
    public Double getStoreDistanceByIdAndLocation(GetStoreQuery query) {
        return storeRepository.getStoreDistanceByIdAndLocation(new GetStoreDetailByIdAndLocationParam(query.storeId(), query.lat(), query.lng()));
    }

    /**
     * 페이징된 가게 목록 조회 (찜 개수 포함, 찜 많은 순 정렬)
     *
     * @param pageable 페이징 정보
     * @return 가게 + 찜 개수 목록
     */
    public Page<StoreWithFavoriteCount> findWithCategoryAndFavoriteCountByOwnerId(FindWithCategoryAndFavoriteCountByOwnerIdQuery query, Pageable pageable) {
        return storeRepository.findWithCategoryAndFavoriteCountByOwnerId(new FindWithCategoryAndFavoriteCountByOwnerIdQuery(query.ownerId()), pageable);
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
     * 페이징된 가게 목록 조회 (찜 개수 포함, 찜 많은 순 정렬)
     *
     * @param pageable 페이징 정보
     * @return 가게 + 찜 개수 목록
     */
    public Page<StoreWithFavoriteCount> findAllWithCategoryAndFavoriteCount(SearchStoresQuery param, Pageable pageable) {
        return storeRepository.findAllWithCategoryAndFavoriteCount(new FindAllWithCategoryAndFavoriteCountParam(param.categoryId(), param.keyword()), pageable);
    }

    /**
     * 가게 목록 조회 (위치 기반 검색)
     * <p>
     * 위치 정보(latitude, longitude, radius)가 모두 제공되면 위치 기반 검색을 수행하고,
     * 하나라도 없으면 전체 가게 목록을 반환합니다.
     * Repository에서 프로젝션과 Store를 각각 조회한 뒤, 서비스에서 조립하여 반환
     * <p>
     * Repository 에서는 데이터를 조회하는 역할만 수행하고,
     * Facade 에서는 Case 에 따른 비즈니스 로직을 수행하고,
     * 조립은 서비스에서 수행하는 것이 더 좋다는 판단함.
     *
     * @param SearchStoresQuery     위치 기반 검색 쿼리
     * @param defaultSearchRadiusKm 기본 반경
     * @param pageable              페이징 정보
     * @return 가게 목록 (Store + 거리 + 찜 개수)
     */
    public Page<StoreWithFavoriteCount> findWithCategoryAndFavoriteCountByLocation(SearchStoresQuery param, double defaultSearchRadiusKm, Pageable pageable) {
        // 파라미터 객체 생성
        FindWithCategoryAndFavoriteCountByLocationParam repoParam = new FindWithCategoryAndFavoriteCountByLocationParam(
                param.lat(), param.lng(), defaultSearchRadiusKm, param.categoryId(), param.keyword());
        // 위치 기반 검색 조회
        Page<StoreWithFavoriteCountByLocationProjection> page = storeRepository.findWithCategoryAndFavoriteCountByLocationProjection(repoParam, pageable);
        // 결과 조회
        List<StoreWithFavoriteCountByLocationProjection> content = page.getContent();

        // 결과가 없으면 빈 페이지 반환
        if (content.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, page.getTotalElements());
        }

        // 가게 ID 목록 조회
        List<Long> ids = content.stream()
                .map(StoreWithFavoriteCountByLocationProjection::getId)
                .toList();

        // 가게 목록 조회
        Map<Long, Store> storeMap = storeRepository.findByIds(ids).stream().collect(Collectors.toMap(Store::getId, s -> s));

        // 가게 목록 데이터 생성
        List<StoreWithFavoriteCount> mapped = content.stream()
                .map(p -> new StoreWithFavoriteCount(
                        storeMap.get(p.getId()), // 가게 엔티티 조회
                        p.getDistance(), // 거리 조회
                        p.getFavoriteCount() != null ? p.getFavoriteCount() : 0L) // 찜 개수 조회
                ) // 가게 목록 조립
                .toList();
        return new PageImpl<>(mapped, pageable, page.getTotalElements());
    }

    /**
     * 사업자 ID 로 가게(Store)만 조회합니다.
     *
     * @param query 사업자 ID 가 포함된 query
     * @return 사업자가 등록한 가게 목록
     */
    public List<Store> findByOwnerId(FindByOwnerIdQuery query) {
        // 도메인의 Repository 파라미터 인스턴스 생성
        FindByOwnerIdParam param = new FindByOwnerIdParam(query.ownerId());

        // DB 에서 가게 조회 및 반환
        return storeRepository.findByOwnerId(param);
    }
}
