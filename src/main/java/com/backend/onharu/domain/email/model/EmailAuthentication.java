package com.backend.onharu.domain.email.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.support.error.CoreException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.backend.onharu.domain.support.error.ErrorType.EmailAuthentication.*;

/**
 * 이메일 인증 엔티티
 * <p>
 * 시스템의 이메일 인증 정보를 나타내는 도메인 입니다.
 * <p>
 * 주요필드:
 * email: 인증 대상인 이메일
 * token: 인증 정보로 사용될 토큰(UUID)
 * isVerified: 인증 여부(기본값 false)
 * expiredAt: 인증 만료 시각
 */
@Getter
@Entity
@Table(name = "EMAIL_AUTHENTICATION")
@NoArgsConstructor
public class EmailAuthentication extends BaseEntity {

    @Column(name = "EMAIL", nullable = false, unique = true)
    private String email; // 인증 이메일

    @Column(name = "TOKEN", nullable = false, length = 36)
    private String token; // 인증 토큰(UUID) -> 향후 이메일 + 토큰 으로 조회 예정

    @Column(name = "IS_VERIFIED")
    private boolean isVerified; // 인증 여부

    @Column(name = "EXPIRED_AT", nullable = false)
    private LocalDateTime expiredAt; // 인증 만료 시각

    @Builder
    public EmailAuthentication(String email, LocalDateTime expiredAt) {
        this.email = email;
        this.token = UUID.randomUUID().toString(); // 인증 토큰은 UUID 로 생성
        this.isVerified = false; // 객체 생성시 미인증 상태
        this.expiredAt = expiredAt;
    }

    /**
     * 이메일 인증 재발급에 사용될 메서드 입니다.
     *
     * @param expiredAt 만료 시각
     */
    public void reissue(LocalDateTime expiredAt) {
        this.token = UUID.randomUUID().toString();
        this.isVerified = false;
        this.expiredAt = expiredAt;
    }

    /**
     * 이메일 인증 토큰이 만료되었는지 확인합니다.
     *
     * @param now 현재 시각
     */
    public boolean isExpired(LocalDateTime now) {
        return expiredAt.isBefore(now);
    }

    /**
     * 이메일 인증을 검증합니다.
     *
     * @param now 현재 시각
     */
    public void verify(LocalDateTime now) {
        if (this.isVerified) { // 이미 검증처리가 된 경우
            throw new CoreException(EMAIL_ALREADY_VERIFIED);
        }

        if (now.isAfter(this.expiredAt)) { // 현재 시각이 인증 만료 시각을 넘은 경우
            throw new CoreException(EMAIL_TOKEN_EXPIRED);
        }

        this.isVerified = true;
    }

    /**
     * 이메일이 검증되었는지 확인합니다.
     */
    public void check() {
        if (!this.isVerified) { // 검증되지 않을 경우
            throw new CoreException(EMAIL_NOT_VERIFIED);
        }
    }
}
