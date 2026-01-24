package com.backend.onharu.domain.support.error;

import lombok.Getter;

/**
 * 애플리케이션의 핵심 비즈니스 로직에서 발생하는 예외를 나타내는 클래스입니다.
 * <p>
 * 이 예외는 IErrorType을 통해 에러 정보를 관리하며,
 * 필요시 추가 데이터를 포함할 수 있습니다.
 * <p>
 * 사용 예시:
 * <pre>
 * throw new CoreException(ErrorType.INVALID_REQUEST);
 * throw new CoreException(ErrorType.KEY_NOT_FOUND_OR_NULL, userId);
 * </pre>
 */
@Getter
public class CoreException extends RuntimeException {

    /**
     * 에러 타입 정보를 담고 있는 객체
     */
    private final IErrorType errorType;
    
    /**
     * 에러와 관련된 추가 데이터 (선택적)
     */
    private final Object data;

    /**
     * 에러 타입과 추가 데이터를 포함하여 예외를 생성합니다.
     *
     * @param errorType 에러 타입
     * @param data      에러와 관련된 추가 데이터
     */
    public CoreException(IErrorType errorType, Object data) {
        super(errorType.getMessage());
        this.errorType = errorType;
        this.data = data;
    }

    /**
     * 에러 타입만으로 예외를 생성합니다.
     *
     * @param errorType 에러 타입
     */
    public CoreException(IErrorType errorType) {
        this(errorType, null);
    }
}
