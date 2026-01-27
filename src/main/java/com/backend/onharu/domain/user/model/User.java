package com.backend.onharu.domain.user.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 엔티티
 * 
 * 시스템의 사용자 정보를 나타내는 도메인 모델입니다.
 * 
 * 주요 필드:
 *  loginId: 로그인에 사용되는 사용자 ID
 *  password: 암호화된 비밀번호
 *  name: 사용자 이름
 *  phone: 전화번호
 *  providerType: 제공자 유형 (LOCAL, KAKAO)
 *  userType: 사용자 유형 (CHILD, OWNER, ADMIN)
 *  status: 계정 상태 (PENDING, ACTIVE, LOCKED, DELETED, BLOCKED)
 */
@Entity
@Table(name = "users")
@EntityListeners(org.springframework.data.jpa.domain.support.AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class User extends BaseEntity {

    @Column(name = "LOGIN_ID", nullable = false, unique = true, length = 50)
    private String loginId;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "NAME", nullable = false, length = 30)
    private String name;

    @Column(name = "PHONE", nullable = false, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "PROVIDER", nullable = false, length = 20)
    private ProviderType providerType;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusType statusType;

    @Builder
    public User(String loginId, String password, String name, String phone, ProviderType providerType, UserType userType, StatusType statusType) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.providerType = providerType;
        this.userType = userType;
        this.statusType = statusType;
    }

    /**
     * 사용자 정보를 업데이트합니다.
     * 
     * @param name 변경할 이름
     * @param phone 변경할 전화번호
     */
    public void update(String name, String phone) {
        this.name = name;
        this.phone = phone;
    }

    /**
     * 비밀번호를 변경합니다.
     * 
     * @param newPassword 새로운 비밀번호
     */
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    /**
     * 계정 상태를 변경합니다.
     * 
     * @param status 변경할 상태
     */
    public void changeStatus(StatusType status) {
        this.statusType = status;
    }
}
