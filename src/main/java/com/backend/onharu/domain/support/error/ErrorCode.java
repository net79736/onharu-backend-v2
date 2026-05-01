package com.backend.onharu.domain.support.error;

public enum ErrorCode {
    BAD_REQUEST, // 400 잘못된 요청
    UNAUTHORIZED, // 401 인증 필요
    FORBIDDEN, // 403 권한 없음
    NOT_FOUND, // 404 리소스를 찾을 수 없음
    CONFLICT, // 409 충돌 (중복 등)
    INTERNAL_SERVER_ERROR, // 500 서버 오류
}
