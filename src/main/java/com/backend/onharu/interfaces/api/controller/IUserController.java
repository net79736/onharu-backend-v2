package com.backend.onharu.interfaces.api.controller;

import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpChildRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpChildResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpOwnerRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.SignUpOwnerResponse;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.UpdateChildProfileRequest;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.UpdateOwnerProfileRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "User", description = "사용자 API")
public interface IUserController {

    /**
     * 아동 회원가입
     * 
     * POST /users/signup/child
     * 아동 회원가입을 진행합니다. 사용자 정보와 증명서 정보를 함께 받습니다.
     */
    @Operation(summary = "아동 회원가입", description = "아동 회원가입을 진행합니다. 사용자 정보와 증명서 정보를 함께 받습니다.")
    ResponseEntity<ResponseDTO<SignUpChildResponse>> signUpChild(
            @RequestBody(
                    description = "아동 회원가입 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpChildRequest.class),
                            examples = @ExampleObject(
                                    name = "아동 회원가입 예시",
                                    value = "{\n" +
                                            "  \"loginId\": \"child123\",\n" +
                                            "  \"password\": \"password123!\",\n" +
                                            "  \"passwordConfirm\": \"password123!\",\n" +
                                            "  \"name\": \"홍길동\",\n" +
                                            "  \"phone\": \"01012345678\",\n" +
                                            "  \"certificate\": \"/certificates/certificate.pdf\"\n" +
                                            "}"
                            )
                    )
            )
            SignUpChildRequest request
    );

    /**
     * 사업자 회원가입
     * 
     * POST /users/signup/owner
     * 사업자 회원가입을 진행합니다. 사용자 정보, 사업자 정보를 함께 받습니다.
     */
    @Operation(summary = "사업자 회원가입", description = "사업자 회원가입을 진행합니다. 사용자 정보, 사업자 정보를 함께 받습니다.")
    ResponseEntity<ResponseDTO<SignUpOwnerResponse>> signUpOwner(
            @RequestBody(
                    description = "사업자 회원가입 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = SignUpOwnerRequest.class),
                            examples = @ExampleObject(
                                    name = "사업자 회원가입 예시",
                                    value = "{\n" +
                                            "  \"loginId\": \"owner123\",\n" +
                                            "  \"password\": \"password123!\",\n" +
                                            "  \"passwordConfirm\": \"password123!\",\n" +
                                            "  \"name\": \"홍길동\",\n" +
                                            "  \"phone\": \"01012345678\",\n" +
                                            "  \"storeName\": \"따뜻한 식당\",\n" +
                                            "  \"businessNumber\": \"1234567890\",\n" +
                                            "  \"levelId\": \"1\"\n" +
                                            "}"
                            )
                    )
            )
            SignUpOwnerRequest request
    );

    /**
     * 사용자 프로필 조회
     * 
     * GET /users/{userId}/profile
     * 사용자 프로필을 조회합니다.
     */
    @Operation(summary = "사용자 프로필 조회", description = "사용자 프로필을 조회합니다.")
    ResponseEntity<ResponseDTO<?>> getProfile(
            @Schema(description = "사용자 ID", example = "1")
            Long userId
    );

    /**
     * 사용자 프로필 수정
     * 
     * PUT /users/{userId}/profile
     * 사용자 프로필을 수정합니다.
     */
    @Operation(summary = "사용자 프로필 수정", description = "사용자 프로필을 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateProfile(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,
            @Schema(description = "아동 프로필 수정 요청")
            UpdateChildProfileRequest childRequest,
            @Schema(description = "사업자 프로필 수정 요청")
            UpdateOwnerProfileRequest ownerRequest
    );

    @Operation(summary = "사용자 회원 탈퇴", description = "사용자 회원 탈퇴를 진행 합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteUser(
            @Schema(description = "사용자 ID", example = "1")
            Long userId
    );
}
