package com.backend.onharu.domain.support.error;

import org.springframework.boot.logging.LogLevel;

/**
 * 에러 타입을 정의하는 인터페이스입니다.
 * <p>
 * 모든 에러 타입은 이 인터페이스를 구현해야 하며,
 * 에러 코드, 메시지, 로그 레벨을 제공해야 합니다.
 * <p>
 * 이 인터페이스를 통해 일관된 에러 처리가 가능합니다.
 */
public interface IErrorType {

    /**
     * 에러 코드를 반환합니다.
     *
     * @return 에러 코드 (ErrorCode enum)
     */
    ErrorCode getCode();

    /**
     * 사용자에게 표시될 에러 메시지를 반환합니다.
     *
     * @return 에러 메시지
     */
    String getMessage();

    /**
     * 로그를 기록할 때 사용할 로그 레벨을 반환합니다.
     *
     * @return 로그 레벨 (ERROR, WARN, INFO 등)
     */
    LogLevel getLogLevel();
}
