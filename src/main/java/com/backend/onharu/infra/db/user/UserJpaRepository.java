package com.backend.onharu.infra.db.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.common.enums.StatusType;
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
     * 로그인 ID LIKE 검색 (대소문자 무시), 본인 제외, 활성 계정만, 최대 20건.
     */
    @Query(
        """
            SELECT u FROM User u 
             WHERE LOWER(u.loginId) LIKE LOWER(CONCAT('%', :loginId, '%'))
               AND u.id != :id 
               AND u.statusType = :statusType 
             ORDER BY u.loginId ASC
             LIMIT 20
        """
    )
    List<User> searchUsersByLoginIdLike(
            String loginId,
            Long id,
            StatusType statusType
    );

    /**
     * 로그인 ID로 사용자 존재 여부를 확인합니다.
     *
     * @param loginId 로그인 ID
     * @return 존재하면 true, 없으면 false
     */
    boolean existsByLoginId(String loginId);

    /**
     * 이름과 전화번호로 사용자를 조회합니다.
     *
     * @param name  사용자 이름
     * @param phone 사용자 전화번호
     * @return 조호된 사용자 엔티티
     */
    Optional<User> findByNameAndPhone(String name, String phone);

    /**
     * 사용자 비밀번호를 임시 비밀번호로 초기화 합니다.
     *
     * @param id       사용자 ID
     * @param password 임시 비밀번호
     */
    @Modifying
    @Query("UPDATE User u SET u.password = :password WHERE u.id = :id")
    void resetPassword(@Param("id") Long id, @Param("password") String password);

    @Modifying
    @Query("UPDATE User u SET u.statusType = :statusType WHERE u.id = :id")
    void updateDeletedUser(@Param("id") Long id, @Param("statusType") StatusType statusType);
}
