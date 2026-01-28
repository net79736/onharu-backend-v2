package com.backend.onharu.domain.store.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가게 태그 조인 엔티티
 * 
 * STORES와 TAGS 테이블 간의 다대다(N:M) 관계를 해소하기 위한 조인 테이블입니다.
 * 하나의 가게는 여러 태그를 가질 수 있고, 하나의 태그는 여러 가게에 사용될 수 있습니다.
 * 
 * 주요 필드:
 *  store: 가게 (FK to STORES)
 *  tag: 태그 (FK to TAGS)
 */
@Entity
@Table(name = "store_tags")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class StoreTag extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    private Store store;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TAG_ID", nullable = false)
    private Tag tag;

    @Builder
    public StoreTag(Store store, Tag tag) {
        this.store = store;
        this.tag = tag;
    }
}
