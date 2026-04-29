package com.backend.onharu.infra.db.notification;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.domain.notification.model.NotificationHistory;

/**
 * 알림 히스토리 JPA Repository
 */
public interface NotificationHistoryJpaRepository extends JpaRepository<NotificationHistory, Long> {

    /**
     * 사용자 ID로 알림 히스토리 목록 조회 (최신순)
     */
    Page<NotificationHistory> findByUser_IdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    /**
     * 사용자 ID로 읽지 않은 알림 히스토리 목록 조회
     */
    @Query("SELECT n FROM NotificationHistory n WHERE n.user.id = :userId AND n.isRead = false")
    List<NotificationHistory> findUnReadedNotificationHistoriesByUserId(@Param("userId") Long userId);

    /**
     * 사용자 ID, 알림 타입, 관련 엔티티 타입, 관련 엔티티 ID로 알림 히스토리 존재 여부 조회
     * @param userId 사용자 ID
     * @param type 알림 타입
     * @param relatedEntityType 관련 엔티티 타입
     * @param relatedEntityId 관련 엔티티 ID
     * @return [true: 존재, false: 존재하지 않음]
     */
    @Query("""
            SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END
            FROM NotificationHistory n
            WHERE n.user.id = :userId
              AND n.type = :type
              AND n.relatedEntityType = :relatedEntityType
              AND n.relatedEntityId = :relatedEntityId
            """)
    boolean existsNotificationHistory(
            @Param("userId") Long userId,
            @Param("type") NotificationHistoryType type,
            @Param("relatedEntityType") String relatedEntityType,
            @Param("relatedEntityId") Long relatedEntityId
    );
}
