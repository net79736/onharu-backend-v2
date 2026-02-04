package com.backend.onharu.infra.security.oauth;

import com.backend.onharu.application.UserFacade;
import com.backend.onharu.domain.common.enums.ProviderType;
import com.backend.onharu.domain.user.dto.UserOAuthCommand;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.security.oauth.dto.OAuthAttributes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * OAuth2 소셜 로그인 서비스
 * <P>
 * 소셜 로그인 제공자로부터 사용자 정보를 성공적으로 가져온 직후 비즈니스 로직을 처리하는 서비스 입니다.
 * </P>
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SocialUserService extends DefaultOAuth2UserService {

    private final UserFacade userFacade;

    /**
     * 소셜 로그인 API 로부터 사용자 정보를 가져오고, 계정이 없는 사용자는 회원가입, 소셜 로그인 계정이 있는 사용자는 로그인을 수행합니다.
     *
     * @param userRequest 소셜 로그인 제공자가 발급한 Client 정보
     * @return OAuth2User 인증된 사용자 정보 객체
     * @throws OAuth2AuthenticationException 인증 실패시 발생
     */
    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("소셜로그인 서비스 호출: {}", userRequest);

        OAuth2User oAuth2User = super.loadUser(userRequest); // 소셜 로그인 제공자로부터 사용자 정보 획득
        Map<String, Object> attributes = oAuth2User.getAttributes(); // 받은 속성 목록

        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId(); // 소셜 로그인 제공자 구분

        ProviderType providerType = ProviderType.fromOAuth2Provider(registrationId); // 사용자(User 도메인) 의 ProviderType 반환

        OAuthAttributes oAuthAttributes = OAuthAttributes.parserProvider(providerType, attributes); // 소셜 로그인에 사용할 공통 DTO 변환 메소드 호출

        User user = userFacade.loginUserOAuth(new UserOAuthCommand.LoginUserOAuthCommand(
                oAuthAttributes.getEmail(), // 소셜 계정의 이메일 == 소셜 사용자 아이디
                oAuthAttributes.getName(),
                oAuthAttributes.getPhone(), // TODO: 카카오 로그인시, 테스트 계정으로 전화번호가 제공되지 않는 오류 발생 -> 해결 필요
                providerType, // 사용자(User 도메인) 의 Provider
                oAuthAttributes.getProviderId() // 소셜 로그인 식별자
        ));

        return new SocialUser(user, attributes); // 사용자(User) 정보를 시큐리티 객체(SocialUser) 반환
    }
}
