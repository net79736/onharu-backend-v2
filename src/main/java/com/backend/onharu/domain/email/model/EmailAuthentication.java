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
import java.util.Random;

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

    @Column(name = "EMAIL", nullable = false)
    private String email; // 인증 이메일

    @Column(name = "TOKEN", nullable = false, length = 36)
    private String token; // 랜덤 숫자 6자리를 입력받도록 변경

    @Column(name = "IS_VERIFIED")
    private boolean isVerified; // 인증 여부

    @Column(name = "EXPIRED_AT", nullable = false)
    private LocalDateTime expiredAt; // 인증 만료 시각

    @Builder
    public EmailAuthentication(String email, LocalDateTime expiredAt) {
        this.email = email;
        this.token = createToken(); // 랜덤 숫자 6자리 토큰
        this.isVerified = false; // 객체 생성시 미인증 상태
        this.expiredAt = expiredAt;
    }

    /**
     * 이메일 인증 재발급에 사용될 메서드 입니다.
     *
     * @param expiredAt 만료 시각
     */
    public void reissue(LocalDateTime expiredAt) {
        this.token = createToken(); // 랜덤 숫자 6자리 토큰
        this.isVerified = false;
        this.expiredAt = expiredAt;
    }

    /**
     * 이메일 인증을 검증합니다.
     * @param token 인증 토큰
     * @param now 현재 시각
     */
    public void verify(String token, LocalDateTime now) {
        if (this.isVerified) { // 이미 검증처리가 된 경우
            throw new CoreException(EMAIL_ALREADY_VERIFIED);
        }

        if (now.isAfter(this.expiredAt)) { // 현재 시각이 인증 만료 시각을 넘은 경우
            throw new CoreException(EMAIL_TOKEN_EXPIRED);
        }

        if (!this.token.equals(token)) { // 입력된 토큰이 인증 토큰과 일치하지 않는 경우
            throw new CoreException(EMAIL_TOKEN_MISMATCH);
        }

        this.isVerified = true;
    }

    /**
     * 인증 토큰용 랜덤 숫자 6자리 생성 메소드 입니다.
     * @return 문자열 타입의 6자리 인증코드
     */
    private String createToken() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        int num = 0;

        // 각 자리별로 랜덤 숫자 생성
        for (int i=0; i<6; i++) {
            num = random.nextInt(10); // 0 부터 9까지 난수 생성
            sb.append(num);
        }

        return sb.toString(); // 숫자를 문자열으로 반환
    }
}
