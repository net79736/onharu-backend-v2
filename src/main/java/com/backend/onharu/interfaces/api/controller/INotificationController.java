package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.GetNotificationResponse;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.UpdateNotificationRequest;
import com.backend.onharu.interfaces.api.dto.NotificationControllerDto.UpdateNotificationResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "알림", description = "알림 API")
public interface INotificationController {

    @Operation(summary = "알림 조회", description = "알림을 조회합니다.")
    ResponseEntity<ResponseDTO<GetNotificationResponse>> getNotification();

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
    
}
