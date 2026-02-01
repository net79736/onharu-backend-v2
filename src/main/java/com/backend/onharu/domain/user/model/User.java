package com.backend.onharu.domain.user.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;

import static com.backend.onharu.domain.support.error.ErrorType.User.*;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<UserOAuth> userOAuth = new ArrayList<>();

    @Column(name = "LOGIN_ID", nullable = false, unique = true, length = 255)
    private String loginId;

    @Column(name = "PASSWORD", nullable = false, length = 255)
    private String password;

    @Column(name = "NAME", nullable = false, length = 30)
    private String name;

    @Column(name = "PHONE", nullable = false, length = 15)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(name = "ROLE", nullable = false, length = 20)
    private UserType userType;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private StatusType statusType;

    @Builder
    public User(String loginId, String password, String name, String phone, UserType userType, StatusType statusType) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.userType = userType;
        this.statusType = statusType;
    }

    /**
     * UserOauth 연결을 호출하는 메서드 입니다.
     */
    public void addUserOAuth(UserOAuth userOAuth) {
        this.userOAuth.add(userOAuth); // User 객체의 필드에 UserOauth 객체를 연결
        userOAuth.addUser(this); // 양방향 연결
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

    /**
     * 사용자 비밀번호를 검증합니다.
     *
     * @param password 로그인할때 입력한 비밀번호
     * @param passwordEncoder 인코더
     */
    public void verifyPassword(String password, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(password, this.password)) {
            throw new CoreException(LOGIN_ID_OR_PASSWORD_MISMATCH);
        }
    }

    /**
     * 사용자 계정 상태를 검증합니다.
     *
     */
    public void verifyStatus() {
        switch (this.statusType) {
            case DELETED -> throw new CoreException(USER_STATUS_DELETED);
            case LOCKED -> throw new CoreException(USER_STATUS_LOCKED);
            case BLOCKED -> throw new CoreException(USER_STATUS_BLOCKED);
        }
    }

    /**
     * 임시로 등록한 사용자 타입을 아동 회원으로 전환합니다.
     */
    public void changeUserTypeToChild() {
        if (!this.userType.equals(UserType.NONE)) {
            throw new CoreException(ErrorType.User.USER_TYPE_NOT_CHANGE);
        }

        this.userType = UserType.CHILD;
    }

    /**
     * 임시로 등록한 사용자 타입을 사업자 회원으로 전환합니다.
     */
    public void changeUserTypeToOwner() {
        if (!this.userType.equals(UserType.NONE)) {
            throw new CoreException(ErrorType.User.USER_TYPE_NOT_CHANGE);
        }

        this.userType = UserType.OWNER;
    }
}
