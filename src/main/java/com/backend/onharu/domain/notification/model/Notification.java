package com.backend.onharu.domain.notification.model;

import jakarta.persistence.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.user.model.User;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 엔티티
 */
@Entity
@Table(name = "notifications")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Notification extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;
    
    @Column(name = "IS_SYSTEM_ENABLED", nullable = false)
    private Boolean isSystemEnabled;  // 시스템 알림 수신 여부

    @Builder
    public Notification(User user, Boolean isSystemEnabled) {
        this.user = user;
        this.isSystemEnabled = Boolean.TRUE.equals(isSystemEnabled);
    }

    /**
     * 시스템 알림 수신 여부를 업데이트합니다.
     * 
     * @param isSystemEnabled 시스템 알림 수신 여부
     */
    public void update(Boolean isSystemEnabled) {
        this.isSystemEnabled = Boolean.TRUE.equals(isSystemEnabled);
    }
}
