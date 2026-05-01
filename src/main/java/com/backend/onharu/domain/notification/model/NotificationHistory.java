package com.backend.onharu.domain.notification.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.user.model.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 알림 히스토리 엔티티
 *
 * 발송 시도한 알림 이벤트를 기록합니다.
 * 예약 상태 변경 등 이벤트에 따른 알림 발송 내역을 조회·관리할 수 있습니다.
 *
 *  user: 수신자 (User)
 *  type: 알림 유형 (예약 확정, 취소 등)
 *  title: 알림 제목
 *  message: 알림 본문
 *  relatedEntityType: 연관 엔티티 타입 (예: RESERVATION)
 *  relatedEntityId: 연관 엔티티 ID
 */
@Entity
@Table(name = "notification_histories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class NotificationHistory extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false, length = 50)
    private NotificationHistoryType type;

    @Column(name = "TITLE", nullable = false, length = 200)
    private String title;

    @Column(name = "MESSAGE", nullable = false)
    private String message;

    @Column(name = "RELATED_ENTITY_TYPE", length = 50)
    private String relatedEntityType;

    @Column(name = "RELATED_ENTITY_ID")
    private Long relatedEntityId;

    @Column(name = "IS_READ", nullable = false)
    private Boolean isRead = false;

    @Builder
    public NotificationHistory(
            User user,
            NotificationHistoryType type,
            String title,
            String message,
            String relatedEntityType,
            Long relatedEntityId,
            String errorMessage,
            Boolean isRead) {
        this.user = user;
        this.type = type;
        this.title = title;
        this.message = message;
        this.relatedEntityType = relatedEntityType;
        this.relatedEntityId = relatedEntityId;
        this.isRead = Boolean.TRUE.equals(isRead);
    }

    /**
     * 알림을 읽음 처리합니다.
     */
    public void markAsRead() {
        this.isRead = true;
    }
}
