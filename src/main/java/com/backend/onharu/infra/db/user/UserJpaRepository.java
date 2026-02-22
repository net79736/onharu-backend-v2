package com.backend.onharu.infra.db.user;

import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.user.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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
    @Query("UPDATE User u SET u.name = :name, u.phone = :phone WHERE u.id = :id")
    void updateUser(@Param("id") Long id, @Param("name") String name, @Param("phone") String phone);

    @Modifying
    @Query("UPDATE User u SET u.statusType = :statusType WHERE u.id = :id")
    void updateDeletedUser(@Param("id") Long id, @Param("statusType")StatusType statusType);
}
