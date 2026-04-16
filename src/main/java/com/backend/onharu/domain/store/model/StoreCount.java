package com.backend.onharu.domain.store.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가게 집계(카운트) 전용 엔티티.
 *
 * <p>Store(기본 정보)와 분리하여 숫자/집계성 데이터를 확장 가능하게 관리합니다.</p>
 * <p>PK=FK(스토어 ID) 구조로 1:1 매핑합니다.</p>
 */
@Entity
@Table(name = "store_counts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class StoreCount {

    @Id
    @Column(name = "STORE_ID", nullable = false)
    private Long storeId;

    @MapsId
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Store store;

    @Column(name = "VIEW_COUNT", nullable = false)
    private Long viewCount = 0L;

    private StoreCount(Store store, long viewCount) {
        this.store = store;
        this.viewCount = Math.max(0L, viewCount);
    }

    public static StoreCount create(Store store) {
        return new StoreCount(store, 0L);
    }

    /**
     * 기존 stores.view_count가 존재하는 환경에서 최초 분리 시,
     * StoreCount row가 없으면 이 메서드로 초기 값을 흡수합니다.
     */
    public static StoreCount migrateFromStore(Store store, long viewCount) {
        return new StoreCount(store, viewCount);
    }
}

