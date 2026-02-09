package com.backend.onharu.infra.db.favorite;

import com.backend.onharu.domain.favorite.model.Favorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 찜하기 JPA Repository
 */
public interface FavoriteJpaRepository extends JpaRepository<Favorite, Long> {

    /**
     * 아동 ID 로 찜하기 목록 조회
     *
     * @param childId 아동 ID
     * @return 아동이 찜한 찜하기 목록
     */
    List<Favorite> findByChild_Id(Long childId);
}
