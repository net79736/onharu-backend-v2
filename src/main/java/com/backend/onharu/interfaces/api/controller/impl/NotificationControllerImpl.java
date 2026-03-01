package com.backend.onharu.interfaces.api.controller.impl;

import static com.backend.onharu.interfaces.api.common.util.PageableUtil.getCurrentPage;

import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.application.NotificationFacade;
import com.backend.onharu.domain.notification.model.Notification;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.common.util.PageableUtil;
import com.backend.onharu.interfaces.api.controller.INotificationController;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.GetNotificationHistoriesRequest;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.GetNotificationHistoriesResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.GetNotificationResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.MarkNotificationReadResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.NotificationHistoryResponse;
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

    @Override
    @GetMapping("/histories")
    public ResponseEntity<ResponseDTO<GetNotificationHistoriesResponse>> getNotificationHistories(
            @ParameterObject @ModelAttribute GetNotificationHistoriesRequest request
    ) {
        Long userId = SecurityUtils.getUserId();
        log.info("getNotificationHistories userId: {}, pageNum: {}", userId, request.pageNum());

        Pageable pageable = PageableUtil.ofOneBased(
                request.pageNum(),
                request.perPage(),
                request.sortField() != null ? request.sortField() : "createdAt",
                request.sortDirection() != null ? request.sortDirection() : "desc"
        );

        Page<NotificationHistory> page = notificationFacade.getNotificationHistories(userId, pageable);
        Page<NotificationHistoryResponse> responsePage = page.map(NotificationHistoryResponse::new);

        GetNotificationHistoriesResponse response = new GetNotificationHistoriesResponse(
                responsePage.getContent(),
                page.getTotalElements(),
                getCurrentPage(responsePage), // 0-based → 1-based 변환
                responsePage.getTotalPages(),
                responsePage.getSize()
        );

        return ResponseEntity.ok(ResponseDTO.success(response));
    }

    @Override
    @PutMapping("/histories/{historyId}/read")
    public ResponseEntity<ResponseDTO<MarkNotificationReadResponse>> markNotificationAsRead(
            @PathVariable("historyId") Long historyId
    ) {
        Long userId = SecurityUtils.getUserId();
        log.info("markNotificationAsRead userId: {}, historyId: {}", userId, historyId);

        NotificationHistory history = notificationFacade.markNotificationHistoryAsRead(userId, historyId); // 알림 읽음 처리
        MarkNotificationReadResponse response = new MarkNotificationReadResponse(new NotificationHistoryResponse(history));

        return ResponseEntity.ok(ResponseDTO.success(response));
    }
}
