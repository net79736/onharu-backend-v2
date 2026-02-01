package com.backend.onharu.infra.db.child;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.child.model.Child;

/**
 * 아동 JPA Repository
 */
public interface ChildJpaRepository extends JpaRepository<Child, Long> {
    /**
     * User의 loginId로 Child 조회
     * 
     * @param loginId 사용자 로그인 ID
     * @return Child (없으면 Optional.empty())
     */
    Optional<Child> findByUser_LoginId(String loginId);
}
