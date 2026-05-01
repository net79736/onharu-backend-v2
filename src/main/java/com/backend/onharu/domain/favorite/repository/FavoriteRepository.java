package com.backend.onharu.domain.favorite.repository;

import com.backend.onharu.domain.favorite.dto.FavoriteRepositoryParam.FindFavoriteByChildIdAndStoreIdParam;
import com.backend.onharu.domain.favorite.dto.FavoriteRepositoryParam.FindFavoritesByChildIdParam;
import com.backend.onharu.domain.favorite.dto.FavoriteRepositoryParam.GetFavoriteByIdParam;
import com.backend.onharu.domain.favorite.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/**
 * 찜하기 Repository 인터페이스
 */
public interface FavoriteRepository {

    /**
     * 찜하기 등록(저장)
     */
    Favorite save(Favorite favorite);

    /**
     * 찜하기 단건 조회
     */
    Favorite getFavorite(GetFavoriteByIdParam favorite);

    /**
     * 아동 ID 로 찜 목록 조회(페이징)
     */
    Page<Favorite> findByChildId(FindFavoritesByChildIdParam param, Pageable pageable);

    /**
     * 찜하기 삭제(취소)
     */
    void delete(Favorite favorite);

    /**
     * 내가 찜한 가게 조회
     */
    Optional<Favorite> findFavoriteByChildIdAndStoreId(FindFavoriteByChildIdAndStoreIdParam param);
}
