package com.backend.onharu.domain.tag.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 태그 엔티티
 * 
 * 가게를 설명하는 태그 정보를 담는 도메인 모델입니다.
 * 
 * 주요 필드:
 *  name: 태그 이름 (예: 커피, 두쫀쿠)
 */
@Entity
@Table(name = "tags")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Tag extends BaseEntity {

    @Column(name = "NAME", nullable = false, length = 50)
    private String name;

    @Builder
    public Tag(String name) {
        this.name = name;
    }

    /**
     * 태그명을 업데이트합니다.
     * 
     * @param name 변경할 태그명
     */
    public void updateName(String name) {
        this.name = name;
    }
}
