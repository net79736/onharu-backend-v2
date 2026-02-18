package com.backend.onharu.domain.favorite.service;

import com.backend.onharu.domain.favorite.dto.FavoriteQuery.FindFavoritesByChildIdQuery;
import com.backend.onharu.domain.favorite.dto.FavoriteQuery.GetFavoriteByIdQuery;
import com.backend.onharu.domain.favorite.dto.FavoriteRepositoryParam.GetFavoriteByIdParam;
import com.backend.onharu.domain.favorite.dto.FavoriteRepositoryParam.FindFavoritesByChildIdParam;
import com.backend.onharu.domain.favorite.model.Favorite;
import com.backend.onharu.domain.favorite.repository.FavoriteRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 찜하기 Query Service
 *
 * 찜하기 도메인의 상태를 조회하는 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
public class FavoriteQueryService {

    private final FavoriteRepository favoriteRepository;

    /**
     * 찜하기 단건 조회
     *
     * @param query 찜하기 ID
     * @return 조회된 Favorite 엔티티 (없으면 FAVORITE_NOT_FOUND 예외 발생)
     */
    public Favorite getFavorite(GetFavoriteByIdQuery query) {
        return favoriteRepository.getFavorite(
                new GetFavoriteByIdParam(query.favoriteId())
        );
    }

    /**
     * 아동 ID 로 찜하기 목록 조회(페이징)
     *
     * @param query 아동 ID
     * @return 아동 ID 에 해당하는 찜하기 리스트
     */
    public Page<Favorite> findFavoritesByChildId(FindFavoritesByChildIdQuery query, Pageable pageable) {
        return favoriteRepository.findByChildId(
                new FindFavoritesByChildIdParam(query.childId()),
                pageable
        );
    }
}
