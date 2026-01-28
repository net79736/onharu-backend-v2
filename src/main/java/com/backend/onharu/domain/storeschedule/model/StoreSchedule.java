package com.backend.onharu.domain.storeschedule.model;

import java.time.LocalTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.DayType;
import com.backend.onharu.domain.store.model.Store;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 가게 일정 엔티티
 */
@Entity
@Table(name = "store_schedules")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class StoreSchedule extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    private Store store;

    @Enumerated(EnumType.STRING)
    @Column(name = "BUSINESS_DAY", nullable = false, length = 3)
    private DayType businessDay;

    @Column(name = "OPEN_TIME", nullable = false, columnDefinition = "TIME")
    private LocalTime openTime;

    @Column(name = "CLOSE_TIME", nullable = false, columnDefinition = "TIME")
    private LocalTime closeTime;
}

