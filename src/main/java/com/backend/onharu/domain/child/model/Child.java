package com.backend.onharu.domain.child.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 아동 엔티티
 */
@Entity
@Table(name = "childrens")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Child extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "CERTIFICATE", nullable = false, length = 255)
    private String certificate;

    @Column(name = "IS_VERIFIED", nullable = false)
    private Boolean isVerified;

    @Builder
    public Child(User user, String certificate, Boolean isVerified) {
        this.user = user;
        this.certificate = certificate;
        this.isVerified = isVerified != null ? isVerified : false;
    }

    /**
     * 아동 정보를 업데이트합니다.
     * 
     * @param certificate 변경할 증명서 파일 경로
     * @param isVerified 변경할 승인 여부
     */
    public void update(String certificate, Boolean isVerified) {
        this.certificate = certificate;
        this.isVerified = isVerified != null ? isVerified : false;
    }

    /**
     * 승인 여부를 업데이트합니다.
     * 
     * @param isVerified 변경할 승인 여부
     */
    public void verify(Boolean isVerified) {
        this.isVerified = isVerified != null ? isVerified : false;
    }
}
