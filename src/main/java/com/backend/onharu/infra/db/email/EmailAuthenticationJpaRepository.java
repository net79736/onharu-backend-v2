package com.backend.onharu.infra.db.email;

import com.backend.onharu.domain.email.model.EmailAuthentication;
import org.springframework.data.jpa.repository.JpaRepository;

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
     * @param email    인증 대상 이메일
     * @return 이메일 인증 엔티티
     */
    boolean existsByEmailAndIsVerifiedTrue(String email);
}
