package com.backend.onharu.domain.notification.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.notification.dto.NotificationHistoryRepositoryParam.FindByUserIdParam;
import com.backend.onharu.domain.notification.dto.NotificationHistoryRepositoryParam.FindUnReadedNotificationHistoriesByUserIdParam;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.domain.notification.repository.NotificationHistoryRepository;

import lombok.RequiredArgsConstructor;

/**
 * 알림 히스토리 조회 전용 서비스
 */
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationHistoryQueryService {

    private final NotificationHistoryRepository notificationHistoryRepository;

    /**
     * 사용자 ID로 알림 히스토리 목록 조회
     *
     * @param userId   사용자 ID (아동/사장 구분 없이 User 기준)
     * @param pageable 페이징·정렬 정보
     * @return 알림 히스토리 페이지
     */
    public Page<NotificationHistory> findPageByUserId(Long userId, Pageable pageable) {
        return notificationHistoryRepository.findByUserId(new FindByUserIdParam(userId, pageable));
    }

    /**
     * 사용자 ID로 읽지 않은 알림 히스토리 목록 조회
     *
     * @param userId 사용자 ID
     * @return 읽지 않은 알림 히스토리 목록
     */
    public List<NotificationHistory> findUnReadedNotificationHistoriesByUserId(Long userId) {
        return notificationHistoryRepository.findUnReadedNotificationHistoriesByUserId(new FindUnReadedNotificationHistoriesByUserIdParam(userId));
    }
}
