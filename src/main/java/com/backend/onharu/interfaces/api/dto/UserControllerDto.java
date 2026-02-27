package com.backend.onharu.interfaces.api.dto;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.common.enums.StatusType;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.interfaces.api.common.dto.ImageMetadataRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public class UserControllerDto {

    public record GetUserResponse(
            UserResponse user
    ) {
    }

    public record UpdateUserRequest(
            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone
    ) {
    }

    public record UserResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long id,

            @Schema(description = "로그인 ID", example = "user123@naver.com")
            String loginId,

            @Schema(description = "사용자 유형", example = "CHILD", allowableValues = {"CHILD", "OWNER", "ADMIN", "NONE"})
            UserType userType,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "상태", example = "ACTIVE", allowableValues = {"PENDING", "ACTIVE", "LOCKED", "DELETED", "BLOCKED"})
            StatusType status
    ) {
    }

    /**
     * 아동 회원가입 요청 DTO
     * 사용자 정보와 증명서 파일을 함께 받습니다.
     */
    public record SignUpChildRequest(
            @NotBlank(message = "아이디는 필수 입력 값 입니다.")
            @Size(min = 8, max = 255, message = "아이디는 최소 8자 이상이어야 합니다.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            @Schema(description = "로그인 아이디", example = "child123@naver.com")
            String loginId,

            @NotBlank(message = "비밀번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,}$",
                    message = "비밀번호는 최소 영문 대소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 하며, 8자 이상이어야 합니다.")
            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @NotBlank(message = "비밀번호 확인은 필수입니다.")
            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "홍길동")
            String name,

            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "닉네임은 필수 입력 값 입니다.")
            @Size(max = 100, message = "닉네임은 최대 100자를 넘을 수 없습니다.")
            @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
            @Schema(description = "닉네임", example = "코끼리땃쥐")
            String nickname,

            @Size(max = 10, message = "이미지는 최대 10개까지 등록할 수 있습니다.")
            @Schema(description = "업로드된 이미지 목록 (Presigned URL 업로드 완료 후 fileKey, filePath)")
            List<ImageMetadataRequest> images
    ) {
    }

    /**
     * 아동 회원가입 응답 DTO
     */
    public record SignUpChildResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "사용자 로그인 아이디", example = "child123@naver.com")
            String loginId
    ) {
    }

    /**
     * 사업자 회원가입 요청 DTO
     * 사용자 정보, 사업자 정보, 사업자 등록 서류 파일을 함께 받습니다.
     */
    public record SignUpOwnerRequest(
            @NotBlank(message = "아이디는 필수 입력 값 입니다.")
            @Size(min = 8, max = 255, message = "아이디는 최소 8자 이상이어야 합니다.")
            @Email(message = "올바른 이메일 형식이 아닙니다.")
            @Schema(description = "로그인 ID", example = "child123@naver.com")
            String loginId,

            @NotBlank(message = "비밀번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,}$",
                    message = "비밀번호는 영문 대문자, 소문자, 숫자, 특수문자를 각각 최소 하나 이상 포함해야 하며 8자 이상이어야 합니다.")
            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @NotBlank(message = "비밀번호 확인은 필수입니다.")
            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "따뜻한 식당")
            String name,

            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "사업자 번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^\\d{10}$", message = "사업자 번호는 숫자 10자리여야 합니다. (예: 1234567890)")
            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    /**
     * 사업자 회원가입 응답 DTO
     */
    public record SignUpOwnerResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long userId
    ) {
    }

    /**
     * 사용자(아동) 프로필 조회 응답 DTO
     */
    public record ChildProfileResponse(
            @Schema(description = "로그인 아이디", example = "child123@naver.com")
            String loginId,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01099992222")
            String phone,

            @Schema(description = "사용자 유형", example = "CHILD")
            UserType userType,

            @Schema(description = "닉네임", example = "코끼리땃쥐")
            String nickname,

            @Schema(description = "첨부 파일 URL 목록", example = "https://minio.example.com/bucket/certificate/certificate.pdf")
            List<String> images
    ) {
    }

    /**
     * 사용자(사업자) 프로필 조회 응답 DTO
     */
    public record OwnerProfileResponse(
            @Schema(description = "로그인 아이디", example = "owner123@naver.com")
            String loginId,

            @Schema(description = "이름", example = "따뜻한 식당")
            String name,

            @Schema(description = "전화번호", example = "01099992222")
            String phone,

            @Schema(description = "사용자 유형", example = "OWNER")
            UserType userType,

            @Schema(description = "등급명", example = "새싹")
            String levelName,

            @Schema(description = "사업자 등록번호", example = "1234567890")
            String businessNumber
    ) {
    }

    /**
     * 아동 프로필 수정 요청 DTO
     * 공통 정보(이름, 전화번호)와 아동 전용 정보(닉네임)를 함께 받습니다.
     */
    public record UpdateChildProfileRequest(
            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "홍길동")
            String name,

            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "닉네임은 필수 입력 값 입니다.")
            @Size(max = 100, message = "닉네임은 최대 100자를 넘을 수 없습니다.")
            @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
            @Schema(description = "닉네임", example = "온하루친구")
            String nickname
    ) {
    }

    /**
     * 사업자 프로필 수정 요청 DTO
     * 공통 정보(이름, 전화번호)와 사업자 전용 정보를 함께 받습니다.
     */
    public record UpdateOwnerProfileRequest(
            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "홍길동")
            String name,

            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "사업자 번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^\\d{10}$", message = "사업자 번호는 숫자 10자리여야 합니다. (예: 1234567890)")
            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    /**
     * 사용자 로그인 요청 DTO
     * 아이디, 비밀번호를 함께 받습니다.
     */
    public record LoginUserRequest(
            @NotBlank(message = "아이디는 필수 입력 값 입니다.")
            @Size(min = 8, max = 50, message = "아이디는 최소 8자 이상이어야 합니다.")
            @Schema(description = "로그인 ID", example = "user123")
            String loginId,

            @NotBlank(message = "비밀번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*\\W).{8,}$",
                    message = "비밀번호는 최소 영문 대소문자, 소문자, 숫자, 특수문자를 각각 하나 이상 포함해야 하며 8자 이상이어야 합니다.")
            @Schema(description = "비밀번호", example = "password123!")
            String password
    ) {
    }

    /**
     * 소셜 사용자(아동) 회원가입 마무리 요청 DTO
     * 전화번호, 닉네임, 증명서 파일을 받습니다.
     */
    public record finishSignUpChildRequest(
            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "닉네임은 필수 입력 값 입니다.")
            @Size(max = 100, message = "닉네임은 최대 100자를 넘을 수 없습니다.")
            @Pattern(regexp = "^[a-zA-Z0-9가-힣]*$", message = "닉네임은 한글, 영문, 숫자만 사용할 수 있습니다.")
            @Schema(description = "닉네임", example = "코끼리땃쥐")
            String nickname,

            @Size(max = 10, message = "이미지는 최대 10개까지 등록할 수 있습니다.")
            @Schema(description = "업로드 완료 된 이미지 목록 (Presigned URL 업로드 완료된 fileKey, filePath)")
            List<ImageMetadataRequest> images
    ) {
    }

    /**
     * 소셜 사용자(사업자) 회원가입 마무리 요청 DTO
     * 전화번호, 매장명, 사업자 등록번호, 등급 정보를 받습니다.
     */
    public record finishSignUpOwnerRequest(
            @NotBlank(message = "전화번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^01(?:0|[1-9])(?:\\d{3}|\\d{4})\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 01012345678)")
            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @NotBlank(message = "이름은 필수 입력 값 입니다.")
            @Size(max = 30, message = "이름은 30자 이내여야 합니다.")
            @Schema(description = "이름", example = "따뜻한 식당")
            String name,

            @NotBlank(message = "사업자 번호는 필수 입력 값 입니다.")
            @Pattern(regexp = "^\\d{10}$", message = "사업자 번호는 숫자 10자리여야 합니다. (예: 1234567890)")
            @Schema(description = "사업자 번호", example = "1234567890")
            String businessNumber
    ) {
    }

    /**
     * 현재 로그인 여부 확인 응답 DTO
     */
    public record MeResponse(
            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "로그인 아이디", example = "child1234@test.com")
            String loginId,

            @Schema(description = "사용자 이름", example = "홍길동")
            String name,

            @Schema(description = "사용자 유형", example = "CHILD")
            UserType userType,

            @Schema(description = "계정 상태", example = "ACTIVE")
            StatusType statusType,

            @Schema(description = "계정 타입", example = "LOCAL")
            ProviderType providerType
    ) {
    }
}
