package com.backend.onharu.domain.support.error;

import org.springframework.boot.logging.LogLevel;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 애플리케이션 전역에서 사용되는 공통 에러 타입을 정의하는 enum입니다.
 * 
 * 각 에러 타입은 ErrorCode, 메시지, 로그 레벨을 포함합니다.
 */
@AllArgsConstructor
@Getter
public enum ErrorType implements IErrorType {
    NOT_FOUND(ErrorCode.NOT_FOUND, "리소스를 찾을 수 없습니다.", LogLevel.ERROR),
    BAD_REQUEST(ErrorCode.BAD_REQUEST, "잘못된 요청입니다.", LogLevel.ERROR),
    FORBIDDEN(ErrorCode.FORBIDDEN, "접근 권한이 없습니다.", LogLevel.WARN),
    INTERNAL_SERVER_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, "서버 오류가 발생했습니다.", LogLevel.ERROR),
    KEY_NOT_FOUND_OR_NULL(ErrorCode.INTERNAL_SERVER_ERROR, "키를 찾을 수 없거나 null입니다.", LogLevel.ERROR);    

    private final ErrorCode code;
    private final String message;
    private final LogLevel logLevel;
}
