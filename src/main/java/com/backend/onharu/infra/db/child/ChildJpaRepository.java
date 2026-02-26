package com.backend.onharu.infra.db.child;

import com.backend.onharu.domain.child.model.Child;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

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

    /**
     * User의 ID로 Child 조회
     * 
     * @param userId 사용자 ID
     * @return Child (없으면 Optional.empty())
     */
    Optional<Child> findByUser_Id(Long userId);

    /**
     * 아동 정보 수정
     *
     * @param id 아동 ID
     * @param nickname 닉네임
     */
    @Query("UPDATE Child c SET c.nickname = :nickname where c.id = :id")
    @Modifying
    void updateChildNicknameById(@Param("id") Long id, @Param("nickname") String nickname);
}
