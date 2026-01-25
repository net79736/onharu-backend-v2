package com.backend.onharu.interfaces.api.common.dto;

/**
 * API 에러 응답을 나타내는 record 클래스입니다.
 * 
 * 모든 에러 응답은 일관된 형식으로 반환되며,
 * 에러 코드와 메시지를 포함합니다.
 * 
 * record를 사용하여 불변 객체로 구현되었습니다.
 *
 * @param success  성공 여부 (예: true, false)
 * @param code    에러 코드 (예: "BAD_REQUEST", "UNAUTHORIZED", "FORBIDDEN", "NOT_FOUND", "CONFLICT", "INTERNAL_SERVER_ERROR")
 * @param message 사용자에게 표시될 에러 메시지
 */
public record ErrorResponse(
        Boolean success,
        String code, 
        String message
) {
}
