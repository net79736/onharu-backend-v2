package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Admin", description = "관리자 API")
public interface IAdminController {

    @Operation(summary = "사업자 회원가입 요청 승인", description = "사업자 회원가입 요청을 승인합니다.")
    ResponseEntity<ResponseDTO<Void>> approveOwnerSignup(
            @Schema(description = "요청 ID", example = "1")
            Long requestId
    );

    @Operation(summary = "사업자 회원가입 요청 거절", description = "사업자 회원가입 요청을 거절합니다.")
    ResponseEntity<ResponseDTO<Void>> rejectOwnerSignup(
            @Schema(description = "요청 ID", example = "1")
            Long requestId
    );

    @Operation(summary = "결식 아동 회원가입 요청 승인", description = "결식 아동 회원가입 요청을 승인합니다.")
    ResponseEntity<ResponseDTO<Void>> approveChildSignup(
            @Schema(description = "요청 ID", example = "1")
            Long requestId
    );

    @Operation(summary = "결식 아동 회원가입 요청 거절", description = "결식 아동 회원가입 요청을 거절합니다.")
    ResponseEntity<ResponseDTO<Void>> rejectChildSignup(
            @Schema(description = "요청 ID", example = "1")
            Long requestId
    );
}
