package com.backend.onharu.domain.notification.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.notification.dto.NotificationCommand.CreateNotificationCommand;
import com.backend.onharu.domain.notification.model.Notification;
import com.backend.onharu.domain.notification.repository.NotificationRepository;
import com.backend.onharu.domain.user.model.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationCommandService {
    private final NotificationRepository notificationRepository;

    /**
     * 알림 생성
     *
     * 주의: User 엔티티는 Facade에서 조회하여 전달해야 합니다.
     */
    public Notification createNotification(CreateNotificationCommand command, User user) {
        Notification notification = Notification.builder()
                .user(user)
                .isSystemEnabled(command.isSystemEnabled())
                .build();

        return notificationRepository.save(notification);
    }

    /**
     * 알림 수정
     *
     * 주의: Notification 엔티티는 Facade에서 조회하여 전달해야 합니다.
     */
    public void updateNotification(Notification notification, Boolean isSystemEnabled) {
        notification.update(isSystemEnabled);
    }
}
