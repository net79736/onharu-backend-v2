package com.backend.onharu.domain.notification.repository;

import org.springframework.data.domain.Page;

import com.backend.onharu.domain.notification.dto.NotificationHistoryRepositoryParam.FindByUserIdParam;
import com.backend.onharu.domain.notification.dto.NotificationHistoryRepositoryParam.GetNotificationHistoryByIdParam;
import com.backend.onharu.domain.notification.model.NotificationHistory;

/**
 * 알림 히스토리 Repository 인터페이스
 *
 * 알림 이벤트 발송 이력을 저장·조회합니다.
 */
public interface NotificationHistoryRepository {

    /**
     * 알림 히스토리 저장
     *
     * @param notificationHistory 저장할 알림 히스토리
     * @return 저장된 엔티티
     */
    NotificationHistory save(NotificationHistory notificationHistory);

    /**
     * 알림 히스토리 단건 조회 (없으면 예외)
     *
     * @param param 조회 파라미터
     * @return 알림 히스토리
     */
    NotificationHistory getNotificationHistory(GetNotificationHistoryByIdParam param);

    /**
     * 사용자 ID로 알림 히스토리 목록 조회 (페이징)
     *
     * @param param 조회 파라미터
     * @return 알림 히스토리 목록 (페이징)
     */
    Page<NotificationHistory> findByUserId(FindByUserIdParam param);
}
