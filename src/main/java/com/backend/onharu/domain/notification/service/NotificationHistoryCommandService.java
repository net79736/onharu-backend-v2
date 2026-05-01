package com.backend.onharu.domain.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.notification.dto.NotificationHistoryRepositoryParam.GetNotificationHistoryByIdParam;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.domain.notification.repository.NotificationHistoryRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

import lombok.RequiredArgsConstructor;

/**
 * 알림 히스토리 변경(쓰기) 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
public class NotificationHistoryCommandService {

    private final NotificationHistoryRepository notificationHistoryRepository;

    /**
     * 알림 히스토리를 읽음 처리합니다.
     *
     * @param historyId 알림 히스토리 ID
     * @param userId    요청한 사용자 ID
     * @return 읽음 처리된 알림 히스토리
     */
    public NotificationHistory markAsRead(Long historyId, Long userId) {
        // 알림 내역 조회
        NotificationHistory history = notificationHistoryRepository.getNotificationHistory(
            new GetNotificationHistoryByIdParam(historyId)
        );
        // 소유자 검증
        if (!history.getUser().getId().equals(userId)) {
            throw new CoreException(ErrorType.Notification.NOTIFICATION_HISTORY_NOT_FOUND);
        }
        // 읽음 처리
        history.markAsRead();
        return notificationHistoryRepository.save(history);
    }
}
