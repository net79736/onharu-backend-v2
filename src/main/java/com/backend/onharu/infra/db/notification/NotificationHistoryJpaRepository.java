package com.backend.onharu.infra.db.notification;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.backend.onharu.domain.notification.model.NotificationHistory;

/**
 * 알림 히스토리 JPA Repository
 */
public interface NotificationHistoryJpaRepository extends JpaRepository<NotificationHistory, Long> {

    /**
     * 사용자 ID로 알림 히스토리 목록 조회 (최신순)
     */
    Page<NotificationHistory> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
