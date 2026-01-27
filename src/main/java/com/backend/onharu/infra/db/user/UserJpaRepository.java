package com.backend.onharu.infra.db.user;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.user.model.User;

/**
 * 사용자 JPA Repository
 */
public interface UserJpaRepository extends JpaRepository<User, Long> {

    /**
     * 로그인 ID로 사용자를 조회합니다.
     * 
     * @param loginId 로그인 ID
     * @return 조회된 사용자 엔티티
     */
    Optional<User> findByLoginId(String loginId);

    /**
     * 로그인 ID로 사용자 존재 여부를 확인합니다.
     * 
     * @param loginId 로그인 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByLoginId(String loginId);
}
