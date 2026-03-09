package com.backend.onharu.infra.db.favorite;

import com.backend.onharu.domain.favorite.model.Favorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 찜하기 JPA Repository
 */
public interface FavoriteJpaRepository extends JpaRepository<Favorite, Long> {

    /**
     * 아동 ID 로 찜하기 목록 조회(페이징)
     *
     * @param childId 아동 ID
     * @return 아동이 찜한 찜하기 목록
     */
    Page<Favorite> findByChild_Id(Long childId, Pageable pageable);

    /**
     * 찜하기 내역 조회
     *
     * @param childId 아동 ID
     * @param storeId 가게 ID
     * @return 특정 아동이 가게에 찜한 찜하기 정보
     */
    Optional<Favorite> findFavoriteByChild_IdAndStore_Id(Long childId, Long storeId);
}
