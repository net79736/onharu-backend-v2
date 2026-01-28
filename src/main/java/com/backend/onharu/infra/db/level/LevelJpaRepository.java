package com.backend.onharu.infra.db.level;

import com.backend.onharu.domain.level.model.Level;
import org.springframework.data.jpa.repository.JpaRepository;

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
}
