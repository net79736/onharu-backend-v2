package com.backend.onharu.domain.user.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.ProviderType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 소셜 로그인 엔티티
 * <p>
 * 시스템의 소셜 로그인에 연동된 사용자 정보를 나타내는 도메인 모델입니다.
 * <p>
 * 주요 필드:
 * providerId: 소셜 로그인 제공자 식별값
 * providerType: 사용자 유형
 */
@Getter
@Entity
@Table(name = "user_oauth", uniqueConstraints = {@UniqueConstraint(columnNames = {"PROVIDER", "PROVIDER_ID"})})
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserOAuth extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Column(name = "PROVIDER_ID", nullable = false, length = 255)
    private String providerId;

    @Builder
    public UserOAuth(String providerId) {
        this.providerId = providerId;
    }

    /**
     * User 객체와 양방향 연결을 위해 추가된 메소드 입니다.
     */
    public void addUser(User user) {
        this.user = user;
    }
}