package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.CreateUserRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.CreateUserResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.GetUserResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.UpdateUserRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User", description = "사용자 API")
public interface IUserController {

    @Operation(summary = "사용자 회원가입", description = "사용자 회원가입을 진행 합니다.")
    ResponseEntity<ResponseDTO<CreateUserResponse>> createUser(
            @Schema(description = "사용자 정보 등록 요청")
            CreateUserRequest request
    );

    @Operation(summary = "사용자 정보 조회", description = "사용자 정보를 조회합니다.")
    ResponseEntity<ResponseDTO<GetUserResponse>> getUser(
            @Schema(description = "사용자 ID", example = "1")
            Long userId
    );

    @Operation(summary = "사용자 정보 수정", description = "사용자 정보를 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateUser(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,
            @Schema(description = "사용자 정보 수정 요청")
            UpdateUserRequest request
    );

    @Operation(summary = "사용자 회원 탈퇴", description = "사용자 회원 탈퇴를 진행 합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteUser(
            @Schema(description = "사용자 ID", example = "1")
            Long userId
    );
}
