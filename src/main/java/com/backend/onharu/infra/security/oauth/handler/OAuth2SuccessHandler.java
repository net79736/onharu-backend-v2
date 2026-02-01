package com.backend.onharu.infra.security.oauth.handler;

import com.backend.onharu.application.UserFacade;
import com.backend.onharu.domain.common.enums.UserType;
import com.backend.onharu.domain.user.model.User;
import com.backend.onharu.infra.security.oauth.SocialUser;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 소셜 로그인 성공시 처리되는 핸들러 입니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final UserFacade userFacade;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("소셜 로그인 성공");

        SocialUser socialUser = (SocialUser) authentication.getPrincipal(); // 인증된 유저 객체
        User user = socialUser.getUser();

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(user, null, socialUser.getAuthorities()); // 세션 설정

        SecurityContextHolder.getContext()
                .setAuthentication(usernamePasswordAuthenticationToken);

        if (user.getUserType().equals(UserType.NONE)) {
            log.info("추가 정보 페이지 호출");
            response.sendRedirect("/users/signup/info"); // 프론트의 추가 정보 입력 페이지로 리다이렉트
        } else {
            log.info("소셜 회원가입 완료");
            response.sendRedirect("/"); // 이미 소셜 회원 가입 완료 → 메인페이지 리다이렉트
        }
    }
}
