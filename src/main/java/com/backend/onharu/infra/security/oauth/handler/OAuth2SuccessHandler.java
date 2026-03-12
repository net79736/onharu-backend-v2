package com.backend.onharu.infra.security.oauth.handler;

import com.backend.onharu.config.ServerUrlProperties;
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
 * <p>
 *     소셜 로그인 인증이 성공적으로 끝난 뒤 실행됩니다.
 *     인증 객체를 변환하여 세션에 저장합니다.
 *     소셜 로그인 사용자가 계정이 없는 경우(NONE 상태) 추가 정보 입력 입력 페이지로 리다이렉트 합니다.
 *     만약, 소셜 로그인 사용자 계정이 있는 경우 메인 페이지로 리다이렉트 합니다.
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final ServerUrlProperties serverUrlProperties;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("소셜 로그인 성공");

        SocialUser socialUser = (SocialUser) authentication.getPrincipal(); // 인증된 유저 객체를 꺼내옵니다.
        User user = socialUser.getUser();

        UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(
                user, // 인증된 사용자(SocialUser) 객체
                null, // 소셜 로그인은 인증 객체 내부에 비밀번호가 필요 없으므로 null
                socialUser.getAuthorities()); // 사용자 권한 목록

        SecurityContextHolder.getContext()
                .setAuthentication(usernamePasswordAuthenticationToken); // 인증 객체를 SecurityContext 저장 (세션 업데이트)

        // 프론트엔드 요청에서 "redirect" 라는 이름의 쿼리파마리터 추출
        String redirect = request.getParameter("redirect");

        if (redirect == null || redirect.isBlank()) {
            redirect = serverUrlProperties.getFront();
        }


        // 소셜 로그인 계정이 없을 경우, 결식 아동/사업자인지 확인하고 해당 회원별 추가정보를 프론트엔드로부터 입력을 받도록 리다이렉트 만약 소셜 로그인 계정이 있다면 메인 페이지로 리다이렉트
        if (user.getUserType().equals(UserType.NONE)) {
            log.info("추가 정보 페이지 호출");
            response.sendRedirect(redirect + "/signup?oauth=" + user.getProviderType().name().toLowerCase());
        } else {
            log.info("소셜 회원가입 완료");
            response.sendRedirect(redirect + "/");
        }
    }
}
