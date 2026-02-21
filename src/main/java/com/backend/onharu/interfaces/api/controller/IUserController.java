package com.backend.onharu.interfaces.api.controller;

import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.UserControllerDto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

@Tag(name = "User", description = "사용자 API")
public interface IUserController {

    /**
     * 아동 회원가입
     * <p>
     * POST /api/users/signup/child
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
                                            "  \"loginId\": \"child123@test.com\",\n" +
                                            "  \"password\": \"password123!\",\n" +
                                            "  \"passwordConfirm\": \"password123!\",\n" +
                                            "  \"name\": \"홍길동\",\n" +
                                            "  \"phone\": \"01012345678\",\n" +
                                            "  \"nickname\": \"코끼리땃쥐\",\n" +
                                            "  \"certificate\": \"/certificates/certificate.pdf\"\n" +
                                            "}"
                            )
                    )
            )
            SignUpChildRequest request
    );

    /**
     * 사업자 회원가입
     * <p>
     * POST /api/users/signup/owner
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
                                            "  \"loginId\": \"owner123@test.com\",\n" +
                                            "  \"password\": \"password123!\",\n" +
                                            "  \"passwordConfirm\": \"password123!\",\n" +
                                            "  \"name\": \"따뜻한 식당\",\n" +
                                            "  \"phone\": \"01012345678\",\n" +
                                            "  \"businessNumber\": \"1234567890\",\n" +
                                            "}"
                            )
                    )
            )
            SignUpOwnerRequest request
    );

    /**
     * 사용자(아동) 프로필 조회 요청
     * GET /api/users/profile/child
     * 세션에 인증된 정보로 아동 프로필을 조회합니다.
     */
    @Operation(summary = "프로필 조회(아동)", description = "현재 세션에 인증된 아동 ID 로 사용자(아동)의 프로필을 조회합니다.")
    ResponseEntity<ResponseDTO<ChildProfileResponse>> getChildProfile();

    /**
     * 사용자(사업자) 프로필 조회 요청
     * GET /api/users/profile/owner
     * 세션에 인증된 정보로 사업자 프로필을 조회합니다.
     */
    @Operation(summary = "프로필 조회(사업자)", description = "현재 세션에 인증된 사업자 ID 로 사용자(사업자)의 프로필을 조회합니다.")
    ResponseEntity<ResponseDTO<OwnerProfileResponse>> getOwnerProfile();

    /**
     * 사용자(아동) 프로필 수정 요청
     * PUT /api/users/profile/child
     */
    @Operation(summary = "프로필 수정(아동)", description = "사용자(아동)의 프로필을 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateChildProfile(
            @RequestBody(
                    description = "아동 프로필 수정 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateChildProfileRequest.class),
                            examples = @ExampleObject(
                                    name = "프로필 수정 요청 예시",
                                    value = """
                                        {
                                          "name": "홍길동",
                                          "phone": "01033337777",
                                          "nickname": "온하루"
                                        }
                                        """
                            )
                    )
            )
            UpdateChildProfileRequest childRequest
    );

    /**
     * 사용자(사업자) 프로필 수정 요청
     * PUT /api/users/profile/owner
     */
    @Operation(summary = "프로필 수정(사업자)", description = "사용자(사업자)의 프로필을 수정합니다.")
    ResponseEntity<ResponseDTO<Void>> updateOwnerProfile(
            @RequestBody(
                    description = "사업자 프로필 수정 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateOwnerProfileRequest.class),
                            examples = @ExampleObject(
                                    name = "프로필 수정 요청 예시",
                                    value = """
                                        {
                                          "name": "홍길동",
                                          "phone": "01033337777",
                                          "levelId": "1",
                                          "businessNumber": "1234567890"
                                        }
                                        """
                            )
                    )
            )
            UpdateOwnerProfileRequest ownerRequest
    );

    /**
     * 사용자 회원 탈퇴 (소프트 삭제)
     * DELETE /api/users
     * 사용자 계정상태를 삭제됨으로 변경합니다.
     */
    @Operation(summary = "사용자 회원 탈퇴", description = "사용자 회원 탈퇴를 진행 합니다.")
    ResponseEntity<ResponseDTO<Void>> deleteUser(
    );

    /**
     * 로컬 사용자 로그인
     * <p>
     * POST /api/users/login
     * 사용자의 아이디와 비밀번호로 로그인을 수행합니다.
     */
    @Operation(summary = "로컬 사용자 로그인", description = "아이디와 비밀번호로 로그인을 수행합니다.")
    ResponseEntity<ResponseDTO<Void>> login(
            @RequestBody(
                    description = "사용자 로그인 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = LoginUserRequest.class),
                            examples = @ExampleObject(
                                    name = "로그인 요청 예시",
                                    value = """
                                        {
                                          "loginId": "child123@test.com",
                                          "password": "password123!"
                                        }
                                        """
                            )
                    )
            )
            LoginUserRequest request,
            HttpServletRequest httpRequest
    );

    /**
     * 사용자 로그아웃
     * <p>
     * POST /api/users/logout
     */
    @Operation(summary = "로컬 사용자 로그아웃", description = "사용자의 로그아웃을 수행 합니다.")
    ResponseEntity<ResponseDTO<Void>> logout(
            HttpServletRequest httpRequest,
            HttpServletResponse httpResponse
    );

    /**
     * 아동 소셜 회원가입
     *
     * POST /api/users/signup/child/finish
     */
    @Operation(summary = "아동 소셜 회원가입 마무리", description = "아동 소셜 회원가입을 마무리합니다. 전화번호, 닉네임, 증명서 파일을 함께 받습니다.")
    ResponseEntity<ResponseDTO<SignUpChildResponse>> finishSignUpChild(
            @AuthenticationPrincipal User user,
            @RequestBody(
                    description = "아동 소셜 회원가입 마무리 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = finishSignUpChildRequest.class),
                            examples = @ExampleObject(
                                    name = "아동 소셜 회원가입 요청",
                                    value = "{\n" +
                                            "  \"phone\": \"01012345678\",\n" +
                                            "  \"nickname\": \"코끼리땃쥐\",\n" +
                                            "  \"certificate\": \"/certificates/certificate.pdf\"\n" +
                                            "}"
                            )
                    )
            )
            finishSignUpChildRequest request
    );

    /**
     * 사업자 소셜 회원가입
     *
     * POST /api/users/signup/owner/finish
     */
    @Operation(summary = "사업자 소셜 회원가입 마무리", description = "사업자 소셜 회원가입을 마무리합니다. ")
    ResponseEntity<ResponseDTO<SignUpChildResponse>> finishSignUpOwner(
            @AuthenticationPrincipal User user,
            @RequestBody(
                    description = "사업자 소셜 회원가입 마무리 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = finishSignUpOwnerRequest.class),
                            examples = @ExampleObject(
                                    name = "사업자 회원가입 예시",
                                    value = "{\n" +
                                            "  \"phone\": \"01012345678\",\n" +
                                            "  \"name\": \"따뜻한 식당\",\n" +
                                            "  \"businessNumber\": \"1234567890\",\n" +
                                            "}"
                            )
                    )
            )
            finishSignUpOwnerRequest request
    );

    /**
     * 로그인 확인
     * <p>
     * GET /api/users/me
     * 세션에 인증된 사용자의 정보를 반환합니다.
     */
    @Operation(summary = "로그인 확인", description = "현재 로그인 여부를 확인하고 사용자 정보를 반환합니다.")
    ResponseEntity<ResponseDTO<MeResponse>> getMe(
    );
}
