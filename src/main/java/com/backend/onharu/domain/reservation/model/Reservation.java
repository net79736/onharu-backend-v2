package com.backend.onharu.domain.reservation.model;

import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_CHILD_ID_MISMATCH;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_NOT_COMPLETED;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_STATUS_CANCELED_ALREADY_CANCELED;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_STATUS_CANNOT_COMPLETE;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_STATUS_CANNOT_CONFIRM;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_STATUS_COMPLETED_CANNOT_CANCEL;
import static com.backend.onharu.domain.support.error.ErrorType.Reservation.RESERVATION_STORE_ID_MISMATCH;
import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.ReservationType;
import com.backend.onharu.domain.common.enums.UserType;
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

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "CANCEL_REQUESTED_BY")
    private UserType cancelRequestedBy;

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
    public void cancel(UserType cancelRequestedBy, String cancelReason) {
        this.status = ReservationType.CANCELED;
        this.cancelRequestedBy = cancelRequestedBy;
        ofNullable(cancelReason).ifPresent(v -> this.cancelReason = v);
    }

    /**
     * 예약을 거절합니다.
     * 
     * @param rejectReason 거절 사유
     */
    public void reject(String cancelReason) {
        this.status = ReservationType.CANCELED;
        this.cancelRequestedBy = UserType.OWNER;
        ofNullable(cancelReason).ifPresent(v -> this.cancelReason = v);
    }

    /**
     * 예약을 확정합니다. (WAITING → CONFIRMED)
     * 사업자가 예약을 승인할 때 사용
     *
     * @throws CoreException RESERVATION_STATUS_CANNOT_CONFIRM 대기 상태가 아닌 예약은 확정할 수 없음
     */
    public void confirm() {
        if (this.status != ReservationType.WAITING) {
            throw new CoreException(RESERVATION_STATUS_CANNOT_CONFIRM);
        }
        this.status = ReservationType.CONFIRMED;
    }

    /**
     * 예약을 완료 처리합니다. (CONFIRMED → COMPLETED)
     * 서비스 이용이 완료되었을 때 사업자가 호출
     *
     * @throws CoreException RESERVATION_STATUS_CANNOT_COMPLETE 확정된 예약만 완료 가능
     */
    public void complete() {
        if (this.status != ReservationType.CONFIRMED) {
            throw new CoreException(RESERVATION_STATUS_CANNOT_COMPLETE);
        }
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
     * 예약을 만료 처리합니다. (WAITING → CANCELED)
     */
    public void expire() {
        this.status = ReservationType.CANCELED;
        this.cancelReason = "예약 시간이 지나 시스템에서 자동 취소되었습니다.";
    }

    /**
     * 예약이 해당 아동에 속하는지 확인합니다.
     *
     * @param childId 아동 ID
     */
    public void belongsToChild(Long childId) {
        if (!this.child.getId().equals(childId)) {
            throw new CoreException(RESERVATION_CHILD_ID_MISMATCH);
        }
    }

    /**
     * 리뷰를 작성 가능한 예약인지 확인합니다.
     *
     * @param childId 아동 ID
     */
    public void verifyWriteable(Long childId) {
        belongsToChild(childId); // 예약이 아동에 속하는지 확인

        if (this.status != ReservationType.COMPLETED) { // 예약 상태가 완료 상태가 아닐 경우
            throw new CoreException(RESERVATION_NOT_COMPLETED);
        }
    }

    /**
     * 예약이 해당 가게에 속하는지 확인합니다.
     *
     * @param storeId 가게 ID
     */
    public void belongsToStore(Long storeId) {
        if (!this.storeSchedule.getStore().getId().equals(storeId)) {
            throw new CoreException(RESERVATION_STORE_ID_MISMATCH);
        }
    }

    /**
     * 예약이 완료 상태인 경우 취소할 수 없도록 예외를 발생시킨다.
     * 
     * (예약 상태가 COMPLETED라면 예외 발생)
     */
    public void validateCancelable() {
        if (this.status == ReservationType.COMPLETED) {
            throw new CoreException(RESERVATION_STATUS_COMPLETED_CANNOT_CANCEL);
        }

        if (this.status == ReservationType.CANCELED) {
            throw new CoreException(RESERVATION_STATUS_CANCELED_ALREADY_CANCELED);
        }
    }

    /**
     * 해당 슬롯이 예약 가능한지 확인합니다.
     * 취소/거절된 예약은 재예약 가능합니다.
     *
     * @return 재예약 가능 여부 (CANCELED, REJECTED인 경우 true)
     */
    public boolean isRebookable() {
        return this.status == ReservationType.CANCELED;
    }

    /**
     * 예약 가능 여부를 확인합니다.
     * 취소/거절 상태일 때만 true (재예약 가능).
     *
     * @return 예약 가능 여부
     */
    public boolean isAvailable() {
        return isRebookable();
    }
}
