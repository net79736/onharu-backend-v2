package com.backend.onharu.infra.security.oauth.handler;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.interfaces.api.common.dto.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 소셜 로그인 실패시 처리되는 핸들러 입니다.
 * <p>
 * 에러 목록을 프론트엔드
 * </p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2FailureHandler implements AuthenticationFailureHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.error("소셜 로그인 실패: ", exception);

        // RestControllerAdvice 에서 캐치할 수 없으므로 예외를 따로 만듦
        CoreException coreException = new CoreException(ErrorType.UserOAuth.USER_O_AUTH_LOGIN_FAILED);
        ErrorResponse errorResponse = new ErrorResponse(
                false,
                coreException.getErrorType().getCode().name(),
                coreException.getErrorType().getMessage()
        );

        // 응답 헤더 설정
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        response.getWriter()
                .write(objectMapper.writeValueAsString(errorResponse)); // 에러 정보를 json 으로 변환해서 전송
    }
}
