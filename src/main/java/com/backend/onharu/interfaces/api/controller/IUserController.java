package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpChildRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpChildResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpOwnerRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpOwnerResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.UpdateChildProfileRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.UpdateOwnerProfileRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User", description = "사용자 API")
public interface IUserController {

    /**
     * 아동 회원가입
     * 
     * POST /users/signup/child
     * 아동 회원가입을 진행합니다. 사용자 정보와 증명서 파일을 함께 받습니다.
     */
    @Operation(summary = "아동 회원가입", description = "아동 회원가입을 진행합니다. 사용자 정보와 증명서 파일을 함께 받습니다.")
    ResponseEntity<ResponseDTO<SignUpChildResponse>> signUpChild(
            @Schema(description = "아동 회원가입 요청")
            SignUpChildRequest request,
            @Schema(description = "증명서 파일")
            MultipartFile certificateFile
    );

    /**
     * 사업자 회원가입
     * 
     * POST /users/signup/owner
     * 사업자 회원가입을 진행합니다. 사용자 정보, 사업자 정보, 사업자 등록 서류 파일을 함께 받습니다.
     */
    @Operation(summary = "사업자 회원가입", description = "사업자 회원가입을 진행합니다. 사용자 정보, 사업자 정보, 사업자 등록 서류 파일을 함께 받습니다.")
    ResponseEntity<ResponseDTO<SignUpOwnerResponse>> signUpOwner(
            @Schema(description = "사업자 회원가입 요청")
            SignUpOwnerRequest request,
            @Schema(description = "사업자 등록 서류 파일")
            MultipartFile businessRegistrationFile
    );

    /**
     * 사용자 프로필 조회
     * 
     * GET /users/{userId}/profile
     * Spring Security Context에서 현재 사용자의 역할을 확인하여 역할별 프로필을 반환합니다.
     */
    @Operation(summary = "사용자 프로필 조회", description = "Spring Security Context에서 현재 사용자의 역할을 확인하여 역할별 프로필을 반환합니다.")
    ResponseEntity<ResponseDTO<?>> getProfile(
            @Schema(description = "사용자 ID", example = "1")
            Long userId
    );

    /**
     * 사용자 프로필 수정
     * 
     * PUT /users/{userId}/profile
     * Spring Security Context에서 현재 사용자의 역할을 확인하여 역할별 프로필을 수정합니다.
     */
    @Operation(summary = "사용자 프로필 수정", description = "Spring Security Context에서 현재 사용자의 역할을 확인하여 역할별 프로필을 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateProfile(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,
            @Schema(description = "아동 프로필 수정 요청 (아동인 경우)")
            UpdateChildProfileRequest childRequest,
            @Schema(description = "사업자 프로필 수정 요청 (사업자인 경우)")
            UpdateOwnerProfileRequest ownerRequest,
            @Schema(description = "증명서 파일 (아동인 경우)")
            MultipartFile certificateFile,
            @Schema(description = "사업자 등록 서류 파일 (사업자인 경우)")
            MultipartFile businessRegistrationFile
    );

    @Operation(summary = "사용자 회원 탈퇴", description = "사용자 회원 탈퇴를 진행 합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteUser(
            @Schema(description = "사용자 ID", example = "1")
            Long userId
    );
}
