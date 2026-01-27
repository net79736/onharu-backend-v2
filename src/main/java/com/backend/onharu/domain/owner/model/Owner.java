package com.backend.onharu.domain.owner.model;

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
 * 사업자 엔티티
 * 
 * 사업자(OWNER) 사용자의 추가 정보를 담는 도메인 모델입니다.
 * USERS 테이블과 1:1 관계를 가지며, 사업자 전용 정보를 저장합니다.
 * 
 * 주요 필드:
 *  userId: 사용자 ID (FK to USERS)
 *  levelId: 등급 ID (FK to LEVELS)
 *  businessNumber: 사업자 번호
 */
@Entity
@Table(name = "owners")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Owner extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "USER_ID", nullable = false, unique = true)
    private User user;

    @Column(name = "LEVEL_ID", nullable = false)
    private Long levelId;

    @Column(name = "BUSINESS_NUMBER", nullable = false, length = 10)
    private String businessNumber;

    @Builder
    public Owner(User user, Long levelId, String businessNumber) {
        this.user = user;
        this.levelId = levelId;
        this.businessNumber = businessNumber;
    }

    /**
     * 사업자 정보를 업데이트합니다.
     * 
     * @param levelId 변경할 등급 ID
     * @param businessNumber 변경할 사업자 번호
     */
    public void update(Long levelId, String businessNumber) {
        this.levelId = levelId;
        this.businessNumber = businessNumber;
    }
}
