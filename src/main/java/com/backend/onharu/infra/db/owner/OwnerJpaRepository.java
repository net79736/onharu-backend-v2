package com.backend.onharu.infra.db.owner;

import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.support.error.CoreException;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 사업자 JPA Repository
 */
public interface OwnerJpaRepository extends JpaRepository<Owner, Long> {
    /**
     * User의 loginId로 Owner 조회
     *
     * @param loginId 사용자 로그인 ID
     * @return Owner
     * @throws CoreException OWNER_NOT_FOUND 조회에 실패할 경우
     */
    Optional<Owner> findByUser_LoginId(String loginId);

    /**
     * User의 ID로 Owner 조회
     *
     * @param userId 사용자 ID
     * @return Owner
     * @throws CoreException OWNER_NOT_FOUND 조회에 실패할 경우
     */
    Optional<Owner> findByUser_Id(Long userId);
}
