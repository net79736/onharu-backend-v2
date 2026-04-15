package com.backend.onharu.infra.db.store;

import java.util.List;
import java.util.Optional;

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
     * 가게 상세 정보 조회
     * COUNT 내에 DISTINCT 사용 이유: 카디널리티 조인 방지를 위함.
     * 
     * @param storeId 가게 ID
     * @return 가게 상세 정보
     */
    @Query(
        value = "SELECT new com.backend.onharu.domain.store.dto.StoreWithFavoriteCount(s, COUNT(DISTINCT f.id)) " +
                "FROM Store s " +
                "JOIN s.category c " +
                "LEFT JOIN Favorite f ON f.store = s " +
                "LEFT JOIN StoreTag st ON st.store = s " +
                "LEFT JOIN Tag t ON t.id = st.tag.id " +
                "WHERE s.id = :storeId " +
                "GROUP BY s.id"
    )
    Optional<StoreWithFavoriteCount> getStoreDetailById(@Param("storeId") Long storeId);

    /**
     * 가게 상세 정보 조회 (위치 기반)
     * 
     * @param storeId 가게 ID
     * @param lat 위도
     * @param lng 경도
     * @return 가게 상세 정보
     */
    @Query(value =
        "SELECT s.id AS id, " +
        "       COALESCE(f_count.cnt, 0) AS favoriteCount, " +
        "       d.distance AS distance " +
        "FROM stores s " +
        "JOIN categories c ON s.category_id = c.id " +
        // 거리 계산을 서브쿼리나 JOIN 절에서 한 번만 수행하도록 최적화
        "JOIN ( " +
        "    SELECT id, " +
        "           (6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * cos(radians(lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(lat)))) AS distance " +
        "    FROM stores " +
        ") d ON s.id = d.id " +
        "LEFT JOIN store_tags st ON st.store_id = s.id " +
        "LEFT JOIN tags t ON t.id = st.tag_id " +
        "LEFT JOIN (SELECT store_id, COUNT(*) as cnt FROM favorites GROUP BY store_id) f_count ON s.id = f_count.store_id " +
        "WHERE s.id = :storeId " + 
        "GROUP BY s.id, f_count.cnt, d.distance", // GROUP BY 규격 맞춤
        nativeQuery = true)
    Optional<StoreWithFavoriteCountByLocationProjection> getStoreDetailByIdAndLocation(@Param("storeId") Long storeId, @Param("lat") Double lat, @Param("lng") Double lng);

    /**
     * 거리만 조회 (가게 상세와 분리)
     *
     * <p>lat/lng가 있을 때 "distance"는 항상 DB에서 계산하도록 분리합니다.</p>
     */
    @Query(value =
        "SELECT " +
        " (6371 * acos(cos(radians(:lat)) * cos(radians(s.lat)) * cos(radians(s.lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(s.lat)))) " +
        "FROM stores s " +
        "WHERE s.id = :storeId",
        nativeQuery = true)
    Double getStoreDistanceByIdAndLocation(@Param("storeId") Long storeId, @Param("lat") Double lat, @Param("lng") Double lng);

    /**
     * 사업자 ID로 페이징된 가게 목록 조회
     * COUNT 내에 DISTINCT 사용 이유: 카디널리티 조인 방지를 위함.
     */
    @Query(
            value = "SELECT new com.backend.onharu.domain.store.dto.StoreWithFavoriteCount(s, COUNT(DISTINCT f.id)) " +
                    "FROM Store s " +
                    "JOIN s.category c " +
                    "LEFT JOIN Favorite f ON f.store = s " +
                    "LEFT JOIN StoreTag st ON st.store = s " +
                    "LEFT JOIN Tag t ON t.id = st.tag.id " +
                    "WHERE s.owner.id = :ownerId " +
                    "GROUP BY s.id",
            countQuery = "SELECT COUNT(s.id) " +
                         "FROM Store s " +
                         "WHERE s.owner.id = :ownerId"
    )
    Page<StoreWithFavoriteCount> findWithCategoryAndFavoriteCountByOwnerId(@Param("ownerId") Long ownerId, Pageable pageable);

    /**
     * 페이징 처리된 가게 목록 조회 (위치 기반이 아닌, 일반 조회)
     * COUNT 내에 DISTINCT 사용 이유: 카디널리티 조인 방지를 위함.
     */
    @Query(
            value = "SELECT new com.backend.onharu.domain.store.dto.StoreWithFavoriteCount(s, COUNT(DISTINCT f.id)) " +
                    "FROM Store s " +
                    "JOIN s.category c " +
                    "LEFT JOIN Favorite f ON f.store = s " +
                    "LEFT JOIN StoreTag st ON st.store = s " +
                    "LEFT JOIN Tag t ON t.id = st.tag.id " +
                    "WHERE 1 = 1 " +
                    "AND (:categoryId IS NULL OR c.id = :categoryId) " +
                    "AND (:keyword IS NULL OR t.name LIKE CONCAT('%', :keyword, '%') OR s.name LIKE CONCAT('%', :keyword, '%')) " +
                    "GROUP BY s ",
            countQuery = "SELECT COUNT(DISTINCT s.id) " +
                         "FROM Store s " +
                         "JOIN s.category c " +
                         "LEFT JOIN StoreTag st ON st.store = s " +
                         "LEFT JOIN Tag t ON t.id = st.tag.id " +
                         "WHERE (:categoryId IS NULL OR c.id = :categoryId) " +
                         "AND (:keyword IS NULL OR t.name LIKE CONCAT('%', :keyword, '%') OR s.name LIKE CONCAT('%', :keyword, '%'))"
    )
    Page<StoreWithFavoriteCount> findAllWithCategoryAndFavoriteCount(@Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);

    /**
     * 페이징된 가게 목록 조회 (위치 기반 검색)
     * 
     * ```sql
     * SELECT *, ( 6371 * acos( cos( radians({}) ) * cos( radians( `lat` ) ) * cos( radians( `lng` ) - radians({}) ) + sin( radians({}) ) * sin( radians( `lat` ) ) ) ) AS distance
     * FROM `positions`
     * HAVING distance <= {}
     * ORDER BY distance ASC
     * ```
     * 참고: https://narup.tistory.com/248
     */
    @Query(value =
        "SELECT s.id AS id, " +
        "       COALESCE(f_count.cnt, 0) AS favoriteCount, " +
        "       d.distance AS distance " +
        "FROM stores s " +
        "JOIN categories c ON s.category_id = c.id " +
        // 거리 계산을 서브쿼리나 JOIN 절에서 한 번만 수행하도록 최적화
        "JOIN ( " +
        "    SELECT id, " +
        "           (6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * cos(radians(lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(lat)))) AS distance " +
        "    FROM stores " +
        ") d ON s.id = d.id " +
        "LEFT JOIN store_tags st ON st.store_id = s.id " +
        "LEFT JOIN tags t ON t.id = st.tag_id " +
        // 찜 개수 조회
        "LEFT JOIN (SELECT store_id, COUNT(*) as cnt FROM favorites GROUP BY store_id) f_count ON f_count.store_id = s.id " +
        "WHERE d.distance <= :radius " +
        "AND (:categoryId IS NULL OR c.id = :categoryId) " +
        "AND (:keyword IS NULL OR t.name LIKE CONCAT('%', :keyword, '%') OR s.name LIKE CONCAT('%', :keyword, '%')) " +
        "GROUP BY s.id, f_count.cnt, d.distance", // GROUP BY 규격 맞춤
        countQuery =
        "SELECT COUNT(DISTINCT s.id) FROM stores s " +
        "JOIN categories c ON s.category_id = c.id " +
        "JOIN ( " +
        "    SELECT id, (6371 * acos(cos(radians(:lat)) * cos(radians(lat)) * cos(radians(lng) - radians(:lng)) + sin(radians(:lat)) * sin(radians(lat)))) AS distance " +
        "    FROM stores " +
        ") d ON s.id = d.id " +
        "LEFT JOIN store_tags st ON st.store_id = s.id " +
        "LEFT JOIN tags t ON t.id = st.tag_id " +
        "WHERE d.distance <= :radius " +
        "AND (:categoryId IS NULL OR c.id = :categoryId) " +
        "AND (:keyword IS NULL OR t.name LIKE CONCAT('%', :keyword, '%') OR s.name LIKE CONCAT('%', :keyword, '%'))",
        nativeQuery = true)
    Page<StoreWithFavoriteCountByLocationProjection> findWithCategoryAndFavoriteCountByLocation(@Param("lat") Double lat, @Param("lng") Double lng, @Param("radius") Double radius, @Param("categoryId") Long categoryId, @Param("keyword") String keyword, Pageable pageable);
    
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
