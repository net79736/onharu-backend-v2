package com.backend.onharu.domain.child.model;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.user.model.User;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 아동 엔티티
 * <p>
 * 주요 필드:
 * - nickname: 닉네임
 * - isVerified: 증명서 승인 여부(처음 아동 엔티티 생성시 false)
 */
@Entity
@Table(name = "children")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Child extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "NICKNAME", nullable = false, unique = true, length = 100)
    private String nickname;

    @Column(name = "IS_VERIFIED", nullable = false)
    private Boolean isVerified;

    @Builder
    public Child(User user, String nickname, Boolean isVerified) {
        this.user = user;
        this.nickname = nickname;
        this.isVerified = isVerified != null ? isVerified : false; // 승인 여부가 비어 있을 경우 false
    }

    /**
     * 아동의 개인 정보를 업데이트합니다.
     *
     * @param nickname 변경할 닉네임
     */
    public void update(String nickname) {
        this.nickname = nickname;
    }

    /**
     * 승인 여부를 업데이트합니다.
     *
     * @param isVerified 변경할 승인 여부
     */
    public void verify(Boolean isVerified) {
        this.isVerified = isVerified;
    }
}
