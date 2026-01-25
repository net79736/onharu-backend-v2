package com.backend.onharu.interfaces.api.support;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorCode;
import com.backend.onharu.interfaces.api.common.dto.ErrorResponse;

import lombok.extern.slf4j.Slf4j;

/**
 * 전역 예외 처리를 담당하는 클래스입니다.
 * <p>
 * 모든 컨트롤러에서 발생하는 예외를 일관된 형식으로 처리하여
 * 클라이언트에게 반환합니다.
 * <p>
 * @RestControllerAdvice 어노테이션을 통해 모든 @RestController에서 발생하는
 * 예외를 처리합니다.
 */
@Slf4j
@RestControllerAdvice
class ApiControllerAdvice extends ResponseEntityExceptionHandler {

    /**
     * CoreException을 처리하는 메서드입니다.
     * <p>
     * 에러 타입에 따라 적절한 로그 레벨로 로깅하고,
     * HTTP 상태 코드를 매핑하여 응답을 반환합니다.
     *
     * @param e 발생한 CoreException
     * @return 에러 응답 (ErrorResponse)와 HTTP 상태 코드를 포함한 ResponseEntity
     */
    @ExceptionHandler(CoreException.class)
    public ResponseEntity<ErrorResponse> handle(CoreException e) {
        final boolean ERROR_RESPONSE_FAILURE = false;

        // 에러 타입의 로그 레벨에 따라 적절한 로그를 기록
        switch (e.getErrorType().getLogLevel()) {
            case ERROR -> log.error("Error occurred: {}", e.getMessage(), e);
            case WARN -> log.warn("Warning: {}", e.getMessage());
            default -> log.info("Info: {}", e.getMessage());
        }

        // 에러 코드에 따라 HTTP 상태 코드를 매핑
        HttpStatus status;
        switch (e.getErrorType().getCode()) {
            case BAD_REQUEST -> status = HttpStatus.BAD_REQUEST; // 400
            case UNAUTHORIZED -> status = HttpStatus.UNAUTHORIZED; // 401
            case FORBIDDEN -> status = HttpStatus.FORBIDDEN; // 403
            case NOT_FOUND -> status = HttpStatus.NOT_FOUND; // 404
            case CONFLICT -> status = HttpStatus.CONFLICT; // 409
            case INTERNAL_SERVER_ERROR -> status = HttpStatus.INTERNAL_SERVER_ERROR; // 500
            default -> status = HttpStatus.OK;
        }

        return new ResponseEntity<>(
                new ErrorResponse(
                        ERROR_RESPONSE_FAILURE, // 실패 응답인 경우 false
                        e.getErrorType().getCode().name(),
                        e.getErrorType().getMessage()
                ),
                status
        );
    }

    /**
     * 처리되지 않은 모든 예외를 처리하는 메서드입니다.
     * <p>
     * 예상치 못한 예외가 발생했을 때 사용되며,
     * 500 Internal Server Error를 반환합니다.
     * <p>
     * 모든 예외를 ERROR 레벨로 로깅하여 디버깅에 도움이 됩니다.
     *
     * @param e 발생한 예외
     * @return 500 Internal Server Error 상태 코드와 에러 응답을 포함한 ResponseEntity
     */
    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.error("Unhandled exception occurred", e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse(false, ErrorCode.INTERNAL_SERVER_ERROR.name(), "에러가 발생했습니다."));
    }
}
