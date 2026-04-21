package com.backend.onharu.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.util.ReflectionTestUtils;

import jakarta.servlet.http.Cookie;

@DisplayName("CookieUtils 단위 테스트")
class CookieUtilsTest {

    private CookieUtils cookieUtils;

    @BeforeEach
    void setUp() {
        cookieUtils = new CookieUtils();
        ReflectionTestUtils.setField(cookieUtils, "isSecure", false);
        ReflectionTestUtils.setField(cookieUtils, "frontDomain", "example.com");
    }

    @Test
    @DisplayName("createCookie(path 포함) — 모든 속성이 세팅된다")
    void createCookieWithPath_setsAllAttributes() {
        Cookie cookie = cookieUtils.createCookie("SESSION", "abc", "/api", 3600, true);

        assertThat(cookie.getName()).isEqualTo("SESSION");
        assertThat(cookie.getValue()).isEqualTo("abc");
        assertThat(cookie.getMaxAge()).isEqualTo(3600);
        assertThat(cookie.getPath()).isEqualTo("/api");
        assertThat(cookie.getDomain()).isEqualTo("example.com");
        assertThat(cookie.isHttpOnly()).isTrue();
        assertThat(cookie.getSecure()).isFalse();
    }

    @Test
    @DisplayName("createCookie(path 없는 오버로드) — 기본 path=/ 적용")
    void createCookieDefaultPath_setsRoot() {
        Cookie cookie = cookieUtils.createCookie("X", "y", 600, false);

        assertThat(cookie.getPath()).isEqualTo("/");
        assertThat(cookie.isHttpOnly()).isFalse();
        assertThat(cookie.getMaxAge()).isEqualTo(600);
    }

    @Test
    @DisplayName("getCookie — 존재하는 쿠키를 Optional 로 반환")
    void getCookie_exists() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("A", "1"), new Cookie("B", "2"));

        Optional<Cookie> found = cookieUtils.getCookie(req, "B");
        assertThat(found).isPresent();
        assertThat(found.get().getValue()).isEqualTo("2");
    }

    @Test
    @DisplayName("getCookie — 쿠키 없으면 empty")
    void getCookie_missing_returnsEmpty() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("A", "1"));

        assertThat(cookieUtils.getCookie(req, "MISSING")).isEmpty();
    }

    @Test
    @DisplayName("getCookie — 요청에 쿠키 자체가 없으면 empty")
    void getCookie_noCookies_returnsEmpty() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        assertThat(cookieUtils.getCookie(req, "X")).isEmpty();
    }

    @Test
    @DisplayName("getCookieValue — 값만 추출한다")
    void getCookieValue_returnsValue() {
        MockHttpServletRequest req = new MockHttpServletRequest();
        req.setCookies(new Cookie("SESSION", "xyz"));

        assertThat(cookieUtils.getCookieValue(req, "SESSION")).contains("xyz");
    }

    @Test
    @DisplayName("deleteCookie(key) — maxAge=0, path=/ 로 설정된 쿠키 반환")
    void deleteCookie_returnsExpired() {
        Cookie cookie = cookieUtils.deleteCookie("SESSION");
        assertThat(cookie.getName()).isEqualTo("SESSION");
        assertThat(cookie.getMaxAge()).isZero();
        assertThat(cookie.getPath()).isEqualTo("/");
    }

    @Test
    @DisplayName("deleteCookie(response, key) — 응답에 expired 쿠키를 추가")
    void deleteCookieResponse_addsExpired() {
        MockHttpServletResponse res = new MockHttpServletResponse();
        cookieUtils.deleteCookie(res, "SESSION");

        Cookie added = res.getCookie("SESSION");
        assertThat(added).isNotNull();
        assertThat(added.getMaxAge()).isZero();
    }

    @Test
    @DisplayName("removeCookie(key, response, path) — domain/secure 속성이 적용된 expired 쿠키 추가")
    void removeCookie_withPath() {
        MockHttpServletResponse res = new MockHttpServletResponse();
        cookieUtils.removeCookie("SESSION", res, "/api/auth");

        Cookie added = res.getCookie("SESSION");
        assertThat(added).isNotNull();
        assertThat(added.getMaxAge()).isZero();
        assertThat(added.getPath()).isEqualTo("/api/auth");
        assertThat(added.getDomain()).isEqualTo("example.com");
    }
}
