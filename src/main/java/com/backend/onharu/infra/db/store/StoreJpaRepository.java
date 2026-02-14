package com.backend.onharu.infra.db.store;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.store.dto.StoreWithFavoriteCount;
import com.backend.onharu.domain.store.dto.StoreWithFavoriteCountByLocationProjection;
import com.backend.onharu.domain.store.model.Store;

/**
 * 가게 JPA Repository
 */
public interface StoreJpaRepository extends JpaRepository<Store, Long> {
    
    /**
     * 페이징된 가게 목록 조회 (카테고리 JOIN + 찜 개수, 찜 많은 순 정렬)
     */
    @Query(
            value = "SELECT new com.backend.onharu.domain.store.dto.StoreWithFavoriteCount(s, COUNT(f)) " +
                    "FROM Store s " +
                    "JOIN s.category c " +
                    "LEFT JOIN Favorite f ON f.store = s " +
                    "WHERE s.owner.id = :ownerId " +
                    "GROUP BY s ",
            countQuery = "SELECT COUNT(s) FROM Store s JOIN category c ON s.category.id = c.id WHERE s.owner.id = :ownerId"
    )
    Page<StoreWithFavoriteCount> findWithCategoryAndFavoriteCountByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    /**
     * 페이징된 가게 목록 조회 (카테고리 JOIN + 찜 개수, 찜 많은 순 정렬)
     * JPQL constructor로 StoreWithFavoriteCount(s, COUNT(f)) 반환.
     */
    @Query(
            value = "SELECT new com.backend.onharu.domain.store.dto.StoreWithFavoriteCount(s, COUNT(f)) " +
                    "FROM Store s " +
                    "JOIN s.category c " +
                    "LEFT JOIN Favorite f ON f.store = s " +                    
                    "WHERE :categoryId IS NULL OR c.id = :categoryId " +
                    "GROUP BY s ",
            countQuery = "SELECT COUNT(s) FROM Store s JOIN s.category c ON s.category.id = c.id WHERE :categoryId IS NULL OR c.id = :categoryId"
    )
    Page<StoreWithFavoriteCount> findAllWithCategoryAndFavoriteCount(@Param("categoryId") Long categoryId, Pageable pageable);

    /**
     * 페이징된 가게 목록 조회 (위치 기반 검색)
     * 
     * Haversine 공식을 사용하여 위치 기반 검색을 수행합니다.
     * 참고: https://narup.tistory.com/248
     * ```sql
     * SELECT *, ( 6371 * acos( cos( radians({$lat}) ) * cos( radians( `lat` ) ) * cos( radians( `lng` ) - radians({$lng}) ) + sin( radians({$lat}) ) * sin( radians( `lat` ) ) ) ) AS distance
     * FROM `positions`
     * HAVING distance <= {$radius}
     * ORDER BY distance ASC
     * ```
     */
    /**
     * 위치 기반 검색 시 categoryId가 null이면 카테고리 조건 없이, 있으면 해당 카테고리로만 필터링.
     */
    @Query(value =
        "SELECT s.id, s.name, f_count.cnt AS favoriteCount, " +
        "(6371 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * cos(radians(s.lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(s.lat)))) AS distance " +
        "FROM stores s " +
        "JOIN categories c ON s.category_id = c.id " +
        "LEFT JOIN (SELECT store_id, COUNT(*) as cnt FROM favorites GROUP BY store_id) f_count ON s.id = f_count.store_id " +
        "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * cos(radians(s.lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(s.lat)))) <= :radius " +
        "AND (:categoryId IS NULL OR c.id = :categoryId) ",
        countQuery =
        "SELECT COUNT(*) FROM stores s " +
        "JOIN categories c ON s.category_id = c.id " +
        "WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * cos(radians(s.lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(s.lat)))) <= :radius " +
        "AND (:categoryId IS NULL OR c.id = :categoryId)",
        nativeQuery = true)
    Page<StoreWithFavoriteCountByLocationProjection> findWithCategoryAndFavoriteCountByLocation(@Param("lat") Double lat, @Param("lng") Double lng, @Param("categoryId") Long categoryId, @Param("radius") Double radius, Pageable pageable);
    
    /**
     * 사업자 ID로 가게 목록 조회
     */
    List<Store> findByOwner_Id(Long ownerId);

    /**
     * 카테고리 ID로 가게 목록 조회
     */
    List<Store> findByCategory_Id(Long categoryId);

    /**
     * 가게 이름으로 검색
     */
    List<Store> findAllByNameContainingIgnoreCase(String name);
}
