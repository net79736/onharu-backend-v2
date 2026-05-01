package com.backend.onharu.interfaces.api.support;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Component;

import com.backend.onharu.utils.CookieUtils;
import com.backend.onharu.utils.SecurityUtils;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 요청 주체(로그인/비로그인)를 식별하고, 비로그인인 경우 clientId 쿠키를 발급/유지합니다.
 *
 * ownerKey 규칙:
 * - 로그인: u:{userId}
 * - 비로그인: c:{clientId}
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ClientIdentityResolver {
    private static final Pattern CLIENT_ID_SAFE = Pattern.compile("^[a-zA-Z0-9._-]{1,64}$");
    private static final String CLIENT_ID_COOKIE_NAME = "X-Client-Id";
    // private static final int CLIENT_ID_COOKIE_MAX_AGE_SECONDS = 60 * 60 * 24 * 30; // 30일
    private static final int CLIENT_ID_COOKIE_MAX_AGE_SECONDS = 30; // 30초

    private final CookieUtils cookieUtils;

    /**
     * 요청 주체(로그인/비로그인)를 식별하고, 비로그인인 경우 clientId 쿠키를 발급/유지합니다.
     * @param request
     * @param response
     * @return u:[:user_id] 또는 c:[:client_id]
     */
    public String resolveOwnerKey(HttpServletRequest request, HttpServletResponse response) {
        Long userId = SecurityUtils.getUserId();
        if (userId != null) {
            return "u:" + userId;
        }

        // 비로그인이면 쿠키 clientId 사용
        String cookieClientId = cookieUtils.getCookieValue(request, CLIENT_ID_COOKIE_NAME).orElse(null);
        String ownerFromCookie = normalizeClientId(cookieClientId);
        if (ownerFromCookie != null) {
            return ownerFromCookie;
        }

        String newClientId = UUID.randomUUID().toString(); // UUID 생성
        if (response != null) {
            response.addCookie(cookieUtils.createCookie(
                    CLIENT_ID_COOKIE_NAME,
                    newClientId,
                    "/",
                    CLIENT_ID_COOKIE_MAX_AGE_SECONDS,
                    true
            ));
        }

        // 새로 만든 값은 normalizeClientId에 의해 항상 c:prefix로 반환됨
        return normalizeClientId(newClientId);
    }

    /**
     * clientId를 정규식에 맞게 정규화합니다.
     * @param clientId
     * @return c:[:client_id]
     */
    private String normalizeClientId(String clientId) {
        if (clientId == null || clientId.isBlank()) {
            return null;
        }
        String trimmed = clientId.trim();
        if (!CLIENT_ID_SAFE.matcher(trimmed).matches()) {
            log.debug("Invalid Client-Id format for anonymous user: {}", trimmed);
            return null;
        }
        return "c:" + trimmed;
    }
}

