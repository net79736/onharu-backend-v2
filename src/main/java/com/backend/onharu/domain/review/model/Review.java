package com.backend.onharu.domain.review.model;

import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.domain.store.model.Store;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * (감사)리뷰 엔티티
 * 아동(Child)은 이용한 가게(Store)에 대해 리뷰를 남깁니다.
 */
@Getter
@Entity
@Table(name = "reviews")
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CHILD_ID", nullable = false)
    private Child child; // 아동(N:1, 단방향)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STORE_ID", nullable = false)
    private Store store; // 가게(N:1, 단방향)

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "RESERVATION_ID", nullable = false, unique = true)
    private Reservation reservation; // 예약(1:1, 단방향)

    @Column(name = "CONTENT", nullable = true)
    private String content; // 리뷰 내용

    @Builder
    public Review(Child child, Store store, Reservation reservation, String content) {
        this.child = child;
        this.store = store;
        this.reservation = reservation;
        this.content = content;
    }
}
