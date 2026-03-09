package com.backend.onharu.infra.db.notification.impl;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.notification.dto.NotificationHistoryRepositoryParam.FindByUserIdParam;
import com.backend.onharu.domain.notification.dto.NotificationHistoryRepositoryParam.FindUnReadedNotificationHistoriesByUserIdParam;
import com.backend.onharu.domain.notification.dto.NotificationHistoryRepositoryParam.GetNotificationHistoryByIdParam;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.domain.notification.repository.NotificationHistoryRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.infra.db.notification.NotificationHistoryJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationHistoryRepositoryImpl implements NotificationHistoryRepository {

    private final NotificationHistoryJpaRepository notificationHistoryJpaRepository;

    @Override
    public NotificationHistory save(NotificationHistory notificationHistory) {
        return notificationHistoryJpaRepository.save(notificationHistory);
    }

    @Override
    public NotificationHistory getNotificationHistory(GetNotificationHistoryByIdParam param) {
        return notificationHistoryJpaRepository.findById(param.id())
            .orElseThrow(() -> new CoreException(ErrorType.Notification.NOTIFICATION_HISTORY_NOT_FOUND));
    }

    @Override
    public Page<NotificationHistory> findByUserId(FindByUserIdParam param) {
        return notificationHistoryJpaRepository.findByUser_IdOrderByCreatedAtDesc(
            param.userId(),
            param.pageable()
        );
    }

    @Override
    public List<NotificationHistory> findUnReadedNotificationHistoriesByUserId(FindUnReadedNotificationHistoriesByUserIdParam param) {
        return notificationHistoryJpaRepository.findUnReadedNotificationHistoriesByUserId(param.userId());
    }
}
