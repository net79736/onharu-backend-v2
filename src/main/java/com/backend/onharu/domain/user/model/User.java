package com.backend.onharu.domain.user.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.support.error.CoreException;
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
 * <p>
 * 시스템의 사용자 정보를 나타내는 도메인 모델입니다.
 * <p>
 * 주요 필드:
 * loginId: 로그인에 사용되는 사용자 ID
 * password: 암호화된 비밀번호
 * name: 사용자 이름
 * phone: 전화번호
 * userType: 사용자 유형 (CHILD, OWNER, ADMIN)
 * status: 계정 상태 (PENDING, ACTIVE, LOCKED, DELETED, BLOCKED)
 * providerType: 제공자 유형 (LOCAL, KAKAO)
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

    @Enumerated(EnumType.STRING)
    @Column(name = "PROVIDER", nullable = false, length = 20)
    private ProviderType providerType;

    @Builder
    public User(String loginId, String password, String name, String phone, UserType userType, StatusType statusType, ProviderType providerType) {
        this.loginId = loginId;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.userType = userType;
        this.statusType = statusType;
        this.providerType = providerType;
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
     * @param name  변경할 이름
     * @param phone 변경할 전화번호
     */
    public void verifyUpdate(String name, String phone) {
        // 사용자 계정 상태 검증
        verifyStatus();

        // 이름 검증
        verifyName(name);

        // 전화번호 검증
        verifyPhone(phone);

        this.name = name;
        this.phone = phone;
    }

    /**
     * 비밀번호를 변경합니다.
     *
     * @param newPassword     새로운 비밀번호
     * @param passwordEncoder 비밀번호 변환 인코더
     */
    public void changePassword(String newPassword, PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(newPassword);
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
     * @param password        로그인할때 입력한 비밀번호
     * @param passwordEncoder 인코더
     */
    public void verifyPassword(String password, PasswordEncoder passwordEncoder) {
        if (!passwordEncoder.matches(password, this.password)) {
            throw new CoreException(LOGIN_ID_OR_PASSWORD_MISMATCH);
        }
    }

    /**
     * 새 비밀번호 확인이 일치하는지 검증합니다.
     *
     * @param password        새 비밀번호
     * @param passwordConfirm 새 비밀번호 확인
     * @throws CoreException PASSWORD_CONFIRM_MISMATCH 두 비밀번호가 일치하지 않는 경우
     */
    public void confirmPassword(String password, String passwordConfirm) {
        if (!password.equals(passwordConfirm)) {
            throw new CoreException(PASSWORD_CONFIRM_MISMATCH);
        }
    }

    /**
     * 사용자 계정 상태를 검증합니다.
     */
    public void verifyStatus() {
        switch (this.statusType) {
            case DELETED -> throw new CoreException(USER_STATUS_DELETED);
            case LOCKED -> throw new CoreException(USER_STATUS_LOCKED);
            case BLOCKED -> throw new CoreException(USER_STATUS_BLOCKED);
        }
    }

    /**
     * 사용자 이름을 검증합니다.
     *
     * @param name 이름
     */
    public void verifyName(String name) {
        // 이름이 비어있는 경우
        if (name == null || name.isBlank()) {
            throw new CoreException(USER_NAME_MUST_NOT_BE_BLANK);
        }

        // 이름 글자를 30자를 초과할 경우
        if (name.length() > 30) {
            throw new CoreException(USER_NAME_MUST_BE_NO_MORE_THAN_30_CHARACTERS_LONG);
        }
    }

    public void verifyPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            throw new CoreException(PHONE_MUST_NOT_BE_BLANK);
        }

        if (!phone.matches("^01[0-9](?:\\d{3,4})\\d{4}$")) {
            throw new CoreException(PHONE_INVALID_FORMAT);
        }
    }

    /**
     * 임시로 등록한 사용자 타입을 아동 회원으로 전환합니다.
     */
    public void changeUserTypeToChild() {
        if (!this.userType.equals(UserType.NONE)) {
            throw new CoreException(USER_TYPE_NOT_CHANGE);
        }

        this.userType = UserType.CHILD;
    }

    /**
     * 임시로 등록한 사용자 타입을 사업자 회원으로 전환합니다.
     */
    public void changeUserTypeToOwner() {
        if (!this.userType.equals(UserType.NONE)) {
            throw new CoreException(USER_TYPE_NOT_CHANGE);
        }

        this.userType = UserType.OWNER;
    }
}
