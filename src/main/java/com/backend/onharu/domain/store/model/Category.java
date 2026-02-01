package com.backend.onharu.domain.store.model;

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
 * 카테고리 엔티티
 * 
 * 가게를 분류하는 카테고리 정보를 담는 도메인 모델입니다.
 * 
 * 주요 필드:
 *  name: 카테고리명 (예: 식당, 카페, 의료, 교육, 생활)
 */
@Entity
@Table(name = "categories")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Category extends BaseEntity {

    @Column(name = "name", nullable = false, length = 30)
    private String name;

    @Builder
    public Category(String name) {
        this.name = name;
    }

    /**
     * 카테고리명을 업데이트합니다.
     * 
     * @param name 변경할 카테고리명
     */
    public void update(String name) {
        this.name = name;
    }
}
