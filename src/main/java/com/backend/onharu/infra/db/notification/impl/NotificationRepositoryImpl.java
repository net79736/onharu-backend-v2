package com.backend.onharu.infra.db.notification.impl;

import java.util.Optional;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.notification.dto.NotificationRepositoryParam.GetNotificationByUserIdParam;
import com.backend.onharu.domain.notification.model.Notification;
import com.backend.onharu.domain.notification.repository.NotificationRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.infra.db.notification.NotificationJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class NotificationRepositoryImpl implements NotificationRepository {
    private final NotificationJpaRepository notificationJpaRepository;

    @Override
    public Notification save(Notification notification) {
        return notificationJpaRepository.save(notification);
    }

    @Override
    public Notification getNotificationByUserId(GetNotificationByUserIdParam param) {
        return findOptionalByUserId(param)
            .orElseThrow(() -> new CoreException(ErrorType.Notification.NOTIFICATION_NOT_FOUND));
    }

    @Override
    public Optional<Notification> findOptionalByUserId(GetNotificationByUserIdParam param) {
        return notificationJpaRepository.findByUser_Id(param.userId());
    }
}
