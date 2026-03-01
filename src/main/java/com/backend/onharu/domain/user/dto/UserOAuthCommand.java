package com.backend.onharu.domain.user.dto;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.file.dto.FileCommand.ImageMetadata;
import com.backend.onharu.domain.user.model.User;

import java.util.List;

/**
 * 소셜 사용자 관련 Command DTO
 */
public class UserOAuthCommand {

    /**
     * 소셜 사용자 생성
     *
     * @param user         연동될 사용자
     * @param providerType 소셜 사용자 타입
     * @param providerId   소셜 사용자 ID
     */
    public record CreateUserOAuth(
            User user,
            ProviderType providerType,
            String providerId
    ) {
    }

    /**
     * 소셜 사용자 로그인 Command
     *
     * @param loginId      로그인 아이디(소셜 이메일)
     * @param name         이름
     * @param phoneNumber  전화번호(미제공시 null 대신에 기본 문자열 삽입)
     * @param providerType 소셜 로그인 타입
     * @param providerId   소셜 로그인 고유 식별번호
     */
    public record LoginUserOAuthCommand(
            String loginId,
            String name,
            String phoneNumber,
            ProviderType providerType,
            String providerId
    ) {
    }

    /**
     * 소셜 사용자(아동) 회원가입 Command
     *
     * @param userId   소셜 사용자 ID
     * @param nickname 닉네임
     * @param images   업로드 된 메타 데이터
     */
    public record SignUpChildUserOAuthCommand(
            String userId,
            String nickname,
            List<ImageMetadata> images
    ) {
    }

    /**
     * 소셜 사용자(사업자) 회원가입 Command
     *
     * @param userId         소셜 사용자 ID
     * @param businessNumber 사업자 등록번호
     */
    public record SignUpOwnerUserOAuthCommand(
            String userId,
            String businessNumber
    ) {
    }
}