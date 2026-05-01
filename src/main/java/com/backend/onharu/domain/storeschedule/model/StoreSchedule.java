package com.backend.onharu.domain.storeschedule.model;

import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.LocalTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.store.model.Store;

import jakarta.persistence.Column;
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

    @Column(name = "SCHEDULE_DATE", nullable = false)
    private LocalDate scheduleDate;

    @Column(name = "START_TIME", nullable = false, columnDefinition = "TIME")
    private LocalTime startTime;

    @Column(name = "END_TIME", nullable = false, columnDefinition = "TIME")
    private LocalTime endTime;

    @Column(name = "MAX_PEOPLE", nullable = false)
    private Integer maxPeople;

    @Builder
    public StoreSchedule(Store store, LocalDate scheduleDate, LocalTime startTime, LocalTime endTime, Integer maxPeople) {
        this.store = store;
        this.scheduleDate = scheduleDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.maxPeople = maxPeople;
    }

    /**
     * 가게 예약 가능 일정을 업데이트합니다.
     * 
     * @param scheduleDate 변경할 일정 날짜
     * @param startTime 변경할 시작 시간
     * @param endTime 변경할 종료 시간
     * @param maxPeople 변경할 최대 인원
     */
    public void update(LocalDate scheduleDate, LocalTime startTime, LocalTime endTime, Integer maxPeople) {
        ofNullable(scheduleDate).ifPresent(v -> this.scheduleDate = v);
        ofNullable(startTime).ifPresent(v -> this.startTime = v);
        ofNullable(endTime).ifPresent(v -> this.endTime = v);
        ofNullable(maxPeople).ifPresent(v -> this.maxPeople = v);
    }

    /**
     * 주어진 시각에 이 일정이 "예약 가능"인지 판정합니다.
     * <p>
     * 규칙:
     * - 일정 날짜가 과거이면 예약 불가
     * - 일정 날짜가 오늘이면, 현재 시각이 {@code startTime} 이전일 때만 예약 가능
     * </p>
     */
    public boolean isBookableAt(LocalDateTime now) {
        LocalDate today = now.toLocalDate();
        if (scheduleDate.isBefore(today)) {
            return false;
        }

        if (scheduleDate.isAfter(today)) {
            return true;
        }

        // scheduleDate == today
        return now.toLocalTime().isBefore(startTime);
    }
}