package com.backend.onharu.infra.db.favorite.impl;

import com.backend.onharu.domain.favorite.dto.FavoriteRepositoryParam;
import com.backend.onharu.domain.favorite.dto.FavoriteRepositoryParam.FindFavoritesByChildIdParam;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.favorite.repository.FavoriteRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.favorite.FavoriteJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.backend.onharu.domain.support.error.ErrorType.Favorite.FAVORITE_NOT_FOUND;


/**
 * 찜하기 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class FavoriteRepositoryImpl implements FavoriteRepository {

    private final FavoriteJpaRepository favoriteJpaRepository;

    @Override
    public Favorite save(Favorite favorite) {
        return favoriteJpaRepository.save(favorite);
    }

    @Override
    public Favorite getFavorite(FavoriteRepositoryParam.GetFavoriteByIdParam favorite) {
        return favoriteJpaRepository.findById(favorite.favoriteId())
                .orElseThrow(() -> new CoreException(FAVORITE_NOT_FOUND));
    }

    @Override
    public List<Favorite> findByChildId(FindFavoritesByChildIdParam param) {
        return favoriteJpaRepository.findByChild_Id(param.childId());
    }

    @Override
    public void delete(Favorite favorite) {
        favoriteJpaRepository.delete(favorite);
    }
}
