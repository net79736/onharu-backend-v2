package com.backend.onharu.infra.db.store.impl;

import static com.backend.onharu.domain.support.error.ErrorType.Store.STORE_NOT_FOUND;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

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
import com.backend.onharu.domain.store.repository.StoreRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.store.StoreJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 가게 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class StoreRepositoryImpl implements StoreRepository {

    private final StoreJpaRepository storeJpaRepository;

    @Override
    public Store save(Store store) {
        return storeJpaRepository.save(store);
    }

    @Override
    public Store getStoreById(GetStoreByIdParam param) {
        return storeJpaRepository.findById(param.storeId())
                .orElseThrow(() -> new CoreException(STORE_NOT_FOUND));
    }

    @Override
    public StoreWithFavoriteCountByLocationProjection getStoreDetailByIdAndLocation(GetStoreDetailByIdAndLocationParam param) {
        return storeJpaRepository.getStoreDetailByIdAndLocation(param.storeId(), param.lat(), param.lng())
                .orElseThrow(() -> new CoreException(STORE_NOT_FOUND));
    }

    @Override
    public Double getStoreDistanceByIdAndLocation(GetStoreDetailByIdAndLocationParam param) {
        Double distance = storeJpaRepository.getStoreDistanceByIdAndLocation(param.storeId(), param.lat(), param.lng());
        if (distance == null) {
            throw new CoreException(STORE_NOT_FOUND);
        }
        return distance;
    }

    @Override
    public StoreWithFavoriteCount getStoreDetailById(GetStoreDetailByIdParam param) {
        return storeJpaRepository.getStoreDetailById(param.storeId())
                .orElseThrow(() -> new CoreException(STORE_NOT_FOUND));
    }

    @Override
    public Page<StoreWithFavoriteCount> findWithCategoryAndFavoriteCountByOwnerId(FindWithCategoryAndFavoriteCountByOwnerIdQuery param, Pageable pageable) {
        return storeJpaRepository.findWithCategoryAndFavoriteCountByOwnerId(param.ownerId(), pageable);
    }

    @Override
    public Page<StoreWithFavoriteCount> findAllWithCategoryAndFavoriteCount(FindAllWithCategoryAndFavoriteCountParam param, Pageable pageable) {
        return storeJpaRepository.findAllWithCategoryAndFavoriteCount(param.categoryId(), param.keyword(), pageable);
    }

    @Override
    public Page<StoreWithFavoriteCountByLocationProjection> findWithCategoryAndFavoriteCountByLocationProjection(FindWithCategoryAndFavoriteCountByLocationParam param, Pageable pageable) {
        return storeJpaRepository.findWithCategoryAndFavoriteCountByLocation(param.lat(), param.lng(), param.radius(), param.categoryId(), param.keyword(), pageable);
    }

    @Override
    public List<Store> findByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return storeJpaRepository.findAllById(ids);
    }

    @Override
    public List<Store> findByOwnerId(FindByOwnerIdParam param) {
        return storeJpaRepository.findByOwner_Id(param.ownerId());
    }

    @Override
    public List<Store> findByCategoryId(FindByCategoryIdParam param) {
        return storeJpaRepository.findByCategory_Id(param.categoryId());
    }

    @Override
    public List<Store> findByName(FindByNameParam param) {
        return storeJpaRepository.findAllByNameContainingIgnoreCase(param.name());
    }

    @Override
    public void delete(Store store) {
        storeJpaRepository.delete(store);
    }
}
