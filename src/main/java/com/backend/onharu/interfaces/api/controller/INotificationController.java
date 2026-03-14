package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.GetNotificationHistoriesRequest;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.GetNotificationHistoriesResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.GetNotificationResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.MarkNotificationReadResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.UpdateNotificationRequest;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.UpdateNotificationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Notification", description = "알림 API")
public interface INotificationController {

    @Operation(summary = "알림 조회", description = "알림 설정을 조회합니다.")
    ResponseEntity<ResponseDTO<GetNotificationResponse>> getNotification();

    @Operation(summary = "알림 내역 목록 조회", description = "현재 사용자의 알림 내역을 페이징하여 조회합니다.")
    ResponseEntity<ResponseDTO<GetNotificationHistoriesResponse>> getNotificationHistories(
        @Schema(description = "페이지 번호, 페이지당 개수, 정렬 필드/방향")
        GetNotificationHistoriesRequest request
    );

    @Operation(
        summary = "알림 읽음 처리",
        description = "알림 히스토리 한 건을 읽음(isRead=true)으로 처리합니다."
    )
    ResponseEntity<ResponseDTO<MarkNotificationReadResponse>> markNotificationAsRead(
        @Parameter(description = "알림 히스토리 ID", example = "1", required = true)
        @PathVariable Long historyId
    );

    @Operation(summary = "알림 수정", description = "알림을 수정합니다.")
    ResponseEntity<ResponseDTO<UpdateNotificationResponse>> updateNotification(
        @Schema(description = "알림 수정 요청")
        @RequestBody(
            description = "알림 수정 요청",
            required = true,
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = UpdateNotificationRequest.class),
                examples = @ExampleObject(
                    name = "알림 수정 요청 예시",
                    value = "{\n" +
                            "  \"isSystemEnabled\": true\n" +
                            "}"
                )
            )
        ) 
        UpdateNotificationRequest request
    );

    @Operation(
        summary = "알림 읽음 처리(전체)",
        description = "알림 히스토리 전체를 읽음(isRead=true)으로 처리합니다."
    )
    @PutMapping("/histories/read/all")
    ResponseEntity<ResponseDTO<Void>> markAllNotificationAsRead();
    
}
