package com.backend.onharu.infra.security.oauth.dto;

import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

/**
 * OAuth2 제공자 관련 DTO
 */
@Getter
@AllArgsConstructor
public class OAuthAttributes {
    private ProviderType providerType;
    private String providerId;
    private String name;
    private String email;
    private String phone;

    /**
     * 소셜 제공자별 호출 메서드
     *
     * @param providerType 소셜 제공자(예: KAKAO, NAVER, GOOGLE 등)
     * @param attributes   소셜 로그인 API 로부터 받은 사용자 정보
     * @return 각 소셜 제공자에 맞게 분리된 OAuthAttributes 객체
     */
    public static OAuthAttributes parserProvider(ProviderType providerType, Map<String, Object> attributes) {

        switch (providerType) {
            case KAKAO -> {
                return kakao(attributes);
            }
            default -> {
                throw new CoreException(ErrorType.UserOAuth.UNSUPPORTED_PROVIDER);
            }
        }
    }

    /**
     * 카카오 로그인 API 정보 변환 메서드
     *
     * @param attributes 카카오 API 로부터 받은 사용자 정보
     * @return OAuth2 제공자 DTO
     */
    public static OAuthAttributes kakao(Map<String, Object> attributes) {

        KakaoResponse response = KakaoResponse.from(attributes);

        return new OAuthAttributes(
                ProviderType.KAKAO,
                response.providerId,
                response.kakaoAccount().name(),
                response.kakaoAccount().email(),
                response.kakaoAccount().phoneNumber()
        );
    }

    /**
     * @param providerId   카카오의 OAuth2 ID
     * @param kakaoAccount 카카오 회원 타입
     */
    public record KakaoResponse(
            String providerId,
            KakaoAccount kakaoAccount
    ) {
        public static KakaoResponse from(Map<String, Object> attributes) {
            return new KakaoResponse(
                    attributes.get("id").toString(),
                    KakaoAccount.from((Map<?, ?>) attributes.get("kakao_account"))
            );
        }

        public record KakaoAccount(
                String name,
                String email,
                String phoneNumber
        ) {
            private static final String DEFAULT_PHONE_NUMBER = "00000000000";

            public static KakaoAccount from(Map<?, ?> map) {
                return new KakaoAccount(
                        (String) map.get("name"),
                        (String) map.get("email"),
                        verifyPhoneNumber((String) map.get("phone_number"))
                );
            }

            /**
             * 카카오 로그인 테스트시, 전화번호를 전송받지 못한 버그가 있기 때문에 아래 메소드를 추가하였습니다.
             */
            private static String verifyPhoneNumber(String phoneNumber) {
                if (phoneNumber == null || phoneNumber.isBlank()) {
                    return DEFAULT_PHONE_NUMBER;
                }

                return phoneNumber;
            }
        }
    }

}
