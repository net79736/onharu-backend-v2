package com.backend.onharu.infra.db.level;

import com.backend.onharu.domain.level.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 등급 JPA Repository
 */
public interface LevelJpaRepository extends JpaRepository<Level, Long> {

    /**
     * 등급명 으로 등급을 조회합니다
     *
     * @param name 등급명
     * @return 조회된 등급 엔티티
     */
    Optional<Level> findByName(String name);

    /**
     * 등급 정보를 업데이트 합니다.
     *
     * @param id   등급 ID
     * @param name 등급명
     * @param conditionNumber 등급 조건 횟수
     */
    @Query("UPDATE Level l SET l.name = :name, l.conditionNumber = :conditionNumber WHERE l.id = :id")
    @Modifying
    void updateNameById(@Param("id") Long id, @Param("name") String name, @Param("conditionNumber") int conditionNumber);

    /**
     * 현재 등급보다 높은 등급을 조회합니다
     *
     * @param conditionNumberIsGreaterThan 현재 등급의 조건 횟수
     * @return 현재 등급보다 높은 조건 횟수를 가진 다음 등급
     */
    Optional<Level> findFirstByConditionNumberGreaterThanOrderByConditionNumberAsc(int conditionNumberIsGreaterThan);
}
