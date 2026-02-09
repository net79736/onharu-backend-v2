package com.backend.onharu.domain.favorite.model;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.store.model.Store;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 찜하기 테이블
 * <p>
 * 결식 아동이 원하는 가게를 찜하기를 나타내는 도메인 모델 입니다.
 */
@Getter
@Entity
@Table(name = "favorites")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Favorite extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_ID", nullable = false)
    private Child child;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    private Store store;

    @Builder
    public Favorite(Child child, Store store) {
        this.child = child;
        this.store = store;
    }
}