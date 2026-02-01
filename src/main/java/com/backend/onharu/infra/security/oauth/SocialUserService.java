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

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SocialUserService extends DefaultOAuth2UserService {

    private final UserFacade userFacade;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("소셜로그인 서비스 호출: {}", userRequest);

        OAuth2User oAuth2User = super.loadUser(userRequest);
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String registrationId = userRequest.getClientRegistration()
                .getRegistrationId();

        ProviderType providerType = ProviderType.fromOAuth2Provider(registrationId);

        OAuthAttributes oAuthAttributes = OAuthAttributes.parserProvider(providerType, attributes);

        User user = userFacade.loginUserOAuth(new UserOAuthCommand.LoginUserOAuthCommand(
                oAuthAttributes.getEmail(), // 소셜 계정의 이메일 == 소셜 사용자 아이디
                oAuthAttributes.getName(),
                oAuthAttributes.getPhone(),
                providerType,
                oAuthAttributes.getProviderId()
        ));

        return new SocialUser(user, attributes);
    }
}
