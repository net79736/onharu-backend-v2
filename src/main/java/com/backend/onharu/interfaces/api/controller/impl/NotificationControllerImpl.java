package com.backend.onharu.interfaces.api.controller.impl;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.application.NotificationFacade;
import com.backend.onharu.domain.notification.model.Notification;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.INotificationController;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.GetNotificationResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.NotificationResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.UpdateNotificationRequest;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.UpdateNotificationResponse;
import com.backend.onharu.utils.SecurityUtils;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationControllerImpl implements INotificationController {
    private final NotificationFacade notificationFacade;

    @Override
    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<GetNotificationResponse>> getNotification() {
        Long userId = SecurityUtils.getUserId();
        log.info("getNotification userId: {}", userId);

        Notification notification = notificationFacade.getNotification(userId); // 알림 조회

        NotificationResponse notificationResponse = new NotificationResponse(notification);

        return ResponseEntity.ok(ResponseDTO.success(new GetNotificationResponse(notificationResponse)));
    }

    @Override
    @PutMapping("/me")
    public ResponseEntity<ResponseDTO<UpdateNotificationResponse>> updateNotification(
            @Valid @RequestBody UpdateNotificationRequest request
    ) {
        Long userId = SecurityUtils.getUserId();
        log.info("updateNotification userId: {}", userId);

        Notification notification = notificationFacade.updateNotification(userId, request); // 알림 수정

        NotificationResponse notificationResponse = new NotificationResponse(notification);

        return ResponseEntity.ok(ResponseDTO.success(new UpdateNotificationResponse(notificationResponse)));
    }
}
