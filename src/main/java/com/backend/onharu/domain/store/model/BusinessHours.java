package com.backend.onharu.domain.store.model;

import java.time.LocalTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.WeekType;

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
 * 가게 영업시간 엔티티
 * 
 * 특정 가게의 요일별 영업 시간을 담는 도메인 모델입니다.
 * 하나의 가게는 여러 요일의 영업 시간을 가질 수 있습니다.
 */
@Entity
@Table(name = "business_hours")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class BusinessHours extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "BUSINESS_DAY", nullable = false, length = 3)
    private WeekType businessDay;

    @Column(name = "OPEN_TIME", nullable = false, columnDefinition = "TIME")
    private LocalTime openTime;

    @Column(name = "CLOSE_TIME", nullable = false, columnDefinition = "TIME")
    private LocalTime closeTime;

    @Builder
    public BusinessHours(Store store, WeekType businessDay, LocalTime openTime, LocalTime closeTime) {
        this.store = store;
        this.businessDay = businessDay;
        this.openTime = openTime;
        this.closeTime = closeTime;
    }

    /**
     * 가게를 설정합니다. (양방향 관계 설정용)
     * 
     * @param store 가게
     */
    public void setStore(Store store) {
        this.store = store;
    }

    /**
     * 영업시간을 업데이트합니다.
     * 
     * @param businessDay 변경할 영업일
     * @param openTime 변경할 오픈 시간
     * @param closeTime 변경할 마감 시간
     */
    public void update(WeekType businessDay, LocalTime openTime, LocalTime closeTime) {
        if (businessDay != null) {
            this.businessDay = businessDay;
        }
        if (openTime != null) {
            this.openTime = openTime;
        }
        if (closeTime != null) {
            this.closeTime = closeTime;
        }
    }
}
