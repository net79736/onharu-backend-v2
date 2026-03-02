package com.backend.onharu.infra.db.notification;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.notification.model.Notification;

/**
 * 알림 JPA Repository
 */
public interface NotificationJpaRepository extends JpaRepository<Notification, Long> {
    /**
     * 사용자 ID로 알림 조회
     */
    Optional<Notification> findByUser_Id(@Param("userId") Long userId);
}
