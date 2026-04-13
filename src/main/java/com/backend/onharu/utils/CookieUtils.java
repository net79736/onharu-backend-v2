package com.backend.onharu.utils;

import java.util.Arrays;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class CookieUtils {
    @Value("${spring.application.is-secure:false}")
    private boolean isSecure;

    @Value("${spring.application.front-domain:localhost}")
    private String frontDomain;

    /**
     * 쿠키 생성
     *
     * @param key        쿠키 키
     * @param value      쿠키 값
     * @param maxAge     쿠키 유효시간 (초 단위)
     * @param path       쿠키 경로
     * @param isHttpOnly JavaScript 에서 접근 불가능하도록 설정
     * @return Cookie
     */
    public Cookie createCookie(String key, String value, String path, int maxAge, boolean isHttpOnly) {
        log.info("createCookie isSecure : {}", isSecure);

        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(isSecure); // Secure 설정 여부 (HTTPS에서만 사용)
        cookie.setPath(path);
        cookie.setDomain(frontDomain);
        cookie.setHttpOnly(isHttpOnly);
        return cookie;
    }

    /**
     * 쿠키 생성
     *
     * @param key        쿠키 키
     * @param value      쿠키 값
     * @param maxAge     쿠키 유효시간 (초 단위)
     * @param isHttpOnly JavaScript 에서 접근 불가능하도록 설정
     * @return Cookie
     */
    public Cookie createCookie(String key, String value, int maxAge, boolean isHttpOnly) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(maxAge);
        cookie.setSecure(isSecure); // Secure 설정 여부 (HTTPS에서만 사용)
        cookie.setPath("/");
        cookie.setHttpOnly(isHttpOnly);
        return cookie;
    }

    /**
     * 쿠키 조회
     *
     * @param request HttpServletRequest
     * @param key     찾고자 하는 쿠키 키
     * @return Optional<Cookie>
     */
    public Optional<Cookie> getCookie(HttpServletRequest request, String key) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(key))
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * 쿠키 값 조회
     */
    public Optional<String> getCookieValue(HttpServletRequest request, String key) {
        return getCookie(request, key).map(Cookie::getValue);
    }

    /**
     * 쿠키 삭제
     *
     * @param key 쿠키 키
     * @return Cookie (삭제 처리된 쿠키)
     */
    public Cookie deleteCookie(String key) {
        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0); // 즉시 만료
        cookie.setPath("/"); // 기존 쿠키 경로와 동일하게 설정
        return cookie;
    }

    /**
     * 쿠키 삭제
     *
     * @param response HttpServletResponse 객체
     * @param path     쿠키 경로
     */
    public void removeCookie(String key, HttpServletResponse response, String path) {
        Cookie cookie = new Cookie(key, null);
        cookie.setMaxAge(0);
        cookie.setPath(path);
        cookie.setDomain(frontDomain);
        cookie.setSecure(isSecure);
        response.addCookie(cookie);
    }

    /**
     * 응답에 쿠키 삭제 추가
     *
     * @param response HttpServletResponse
     * @param key      삭제할 쿠키 키
     */
    public void deleteCookie(HttpServletResponse response, String key) {
        Cookie cookie = deleteCookie(key);
        response.addCookie(cookie);
    }
}
