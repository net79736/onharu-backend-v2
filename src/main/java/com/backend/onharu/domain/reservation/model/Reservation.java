package com.backend.onharu.domain.reservation.model;

import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_CHILD_ID_MISMATCH;
import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.support.error.CoreException;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 예약 엔티티
 * 
 * 아동이 가게에 예약한 정보를 담는 도메인 모델
 */
@Entity
@Table(name = "reservations")
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class Reservation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_ID", nullable = false)
    private Child child;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_SCHEDULE_ID", nullable = false)
    private StoreSchedule storeSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "STATUS", nullable = false, length = 20)
    private ReservationType status = ReservationType.WAITING;

    @Column(name = "PEOPLE", nullable = false)
    private Integer people;

    @Column(name = "RESERVATION_AT", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime reservationAt;

    @Column(name = "CANCEL_REASON", columnDefinition = "TEXT")
    private String cancelReason;

    @Column(name = "REJECT_REASON", columnDefinition = "TEXT")
    private String rejectReason;

    @Builder
    public Reservation(Child child, StoreSchedule storeSchedule, Integer people, 
                      LocalDateTime reservationAt, ReservationType status) {
        this.child = child;
        this.storeSchedule = storeSchedule;
        this.people = people;
        this.reservationAt = reservationAt != null ? reservationAt : LocalDateTime.now();
        this.status = status != null ? status : ReservationType.WAITING;
    }

    /**
     * 예약을 취소합니다.
     * 
     * @param cancelReason 취소 사유
     */
    public void cancel(String cancelReason) {
        this.status = ReservationType.CANCELED;
        ofNullable(cancelReason).ifPresent(v -> this.cancelReason = v);
    }

    /**
     * 예약을 거절합니다.
     * 
     * @param rejectReason 거절 사유
     */
    public void reject(String rejectReason) {
        this.status = ReservationType.REJECTED;
        ofNullable(rejectReason).ifPresent(v -> this.rejectReason = v);
    }

    /**
     * 예약을 완료 처리합니다.
     */
    public void complete() {
        this.status = ReservationType.COMPLETED;
    }

    /**
     * 예약 상태를 변경합니다.
     * 
     * @param status 변경할 상태
     */
    public void changeStatus(ReservationType status) {
        this.status = status;
    }

    /**
     * 취소 사유를 업데이트합니다.
     * 
     * @param cancelReason 취소 사유
     */
    public void updateCancelReason(String cancelReason) {
        this.cancelReason = cancelReason;
    }

    /**
     * 예약이 해당 아동에 속하는지 확인합니다.
     * 
     * @param childId 아동 ID
     */
    public void BelongsTo(Long childId) {
        if (!this.child.getId().equals(childId)) {
            throw new CoreException(RESERVATION_CHILD_ID_MISMATCH);
        }
    }
}
