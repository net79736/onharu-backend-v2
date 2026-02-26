package com.backend.onharu.infra.db.owner;

import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.support.error.CoreException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    /**
     * 사업자 정보 수정
     *
     * @param businessNumber 사업자 등록번호
     * @param id             사업자 ID
     */
    @Modifying
    @Query("UPDATE Owner o SET o.businessNumber = :businessNumber WHERE o.id = :id")
    void updateOwnerBusinessNumberById(@Param("businessNumber") String businessNumber, @Param("id") Long id);
}
