package com.backend.onharu.domain.notification.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.notification.dto.NotificationQuery.GetNotificationByUserIdQuery;
import com.backend.onharu.domain.notification.dto.NotificationRepositoryParam.GetNotificationByUserIdParam;
import com.backend.onharu.domain.notification.model.Notification;
import com.backend.onharu.domain.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationQueryService {
    private final NotificationRepository notificationRepository;

    /**
     * 알림 조회 (없으면 예외)
     */
    public Notification getNotificationByUserId(GetNotificationByUserIdQuery query) {
        return notificationRepository.getNotificationByUserId(new GetNotificationByUserIdParam(query.userId()));
    }

    /**
     * 알림 조회
     */
    public Optional<Notification> findOptionalByUserId(GetNotificationByUserIdQuery query) {
        return notificationRepository.findOptionalByUserId(new GetNotificationByUserIdParam(query.userId()));
    }
}
