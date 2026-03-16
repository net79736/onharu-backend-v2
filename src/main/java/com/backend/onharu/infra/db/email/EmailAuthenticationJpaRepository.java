package com.backend.onharu.infra.db.email;

import com.backend.onharu.domain.email.model.EmailAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 이메일 인증 JPA Repository
 */
public interface EmailAuthenticationJpaRepository extends JpaRepository<EmailAuthentication, Long> {

    /**
     * 이메일 로 인증 엔티티를 조회합니다.
     *
     * @param email 인증 대상 이메일
     * @return 이메일 인증 엔티티
     */
    Optional<EmailAuthentication> findByEmail(String email);

    /**
     * 이메일과 토큰 정보로 인증 엔티티를 조회합니다.
     *
     * @param email 인증 대상 이메일
     * @param token 인증 토큰
     * @return 이메일 인증 엔티티
     */
    Optional<EmailAuthentication> findByEmailAndToken(String email, String token);

    /**
     * 현재 시간을 기준으로 만료된 인증 엔티티 목록을 조회합니다.
     *
     * @param now 현재 시각
     * @return 이메일 인증 엔티티
     */
    List<EmailAuthentication> findAllByExpiredAtBefore(LocalDateTime now);

    /**
     * 이메일 인증 완료 여부를 확인합니다.
     *
     * @param email 인증 대상 이메일
     * @return 이메일 인증 엔티티
     */
    boolean existsByEmailAndIsVerifiedTrue(String email);

    /**
     * 이메일로 마지막에 생성된 이메일 인증 엔티티 조회
     *
     * @param email 이메일
     * @return 이메일 인증 엔티티
     */
    Optional<EmailAuthentication> findTopByEmailOrderByCreatedAtDesc(String email);

    /**
     * 현재 시간을 기준으로 미인증 토큰을 만료 처리합니다.
     *
     * @param email 이메일
     * @param now   현재 시각
     */
    @Modifying
    @Query("""
            UPDATE EmailAuthentication e SET e.expiredAt = :now
            WHERE e.email = :email AND e.isVerified = false AND e.expiredAt > :now
            """)
    void expireTokens(@Param("email") String email, @Param("now") LocalDateTime now);

    /**
     * 현재 시간을 기준으로 이메일 인증이 몇번 호출되는지 계산합니다.
     * @param email 이메일
     * @param now 현재 시각
     * @return 이메일 호출 횟수
     */
    @Query("""
            SELECT COUNT(e)
            FROM EmailAuthentication e
            WHERE e.email = :email
            AND e.createdAt > :now
            """)
    long countEmailAuthentication(@Param("email") String email, @Param("now") LocalDateTime now);
}
