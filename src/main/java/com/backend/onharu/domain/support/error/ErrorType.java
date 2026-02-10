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
    
    /**
     * 파일 업로드/다운로드 관련 에러 타입
     */
    @AllArgsConstructor
    public enum FileOperation implements IErrorType {
        FILE_NOT_FOUND(ErrorCode.NOT_FOUND, "파일을 찾을 수 없습니다.", LogLevel.ERROR),
        FILE_DOWNLOAD_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, "파일 다운로드 중 오류가 발생했습니다.", LogLevel.ERROR),
        FILE_UPLOAD_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, "파일 업로드 중 오류가 발생했습니다.", LogLevel.ERROR),
        FILE_UPLOAD_INVALID_MIME_TYPE_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, "유효하지 않은 MIME 타입 또는 확장자입니다.", LogLevel.ERROR),
        FILE_DELETE_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, "파일 삭제 중 오류가 발생했습니다.", LogLevel.ERROR),
        MINIO_CLIENT_ERROR(ErrorCode.INTERNAL_SERVER_ERROR, "MinIO 클라이언트 오류가 발생했습니다.", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 이메일 인증 관련 에러 타입
     */
    @AllArgsConstructor
    public enum EmailAuthentication implements IErrorType {
        EMAIL_AUTHENTICATION_NOT_FOUND(ErrorCode.BAD_REQUEST, "이메일 인증을 찾을 수 없습니다.", LogLevel.ERROR),
        EMAIL_ALREADY_VERIFIED(ErrorCode.BAD_REQUEST, "이미 인증이 완료된 이메일 입니다.", LogLevel.ERROR),
        EMAIL_TOKEN_EXPIRED(ErrorCode.FORBIDDEN, "이메일 인증 토큰이 만료되었습니다.", LogLevel.ERROR),
        EMAIL_NOT_VERIFIED(ErrorCode.FORBIDDEN, "이메일 인증 되지 않았습니다.", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 사용자 관련 에러 타입
     */
    @AllArgsConstructor
    public enum User implements IErrorType {
        USER_NOT_FOUND(ErrorCode.NOT_FOUND, "사용자를 찾을 수 없습니다.", LogLevel.ERROR),
        USER_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "사용자 ID는 필수입니다.", LogLevel.ERROR),
        LOGIN_ID_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "로그인 ID는 필수입니다.", LogLevel.ERROR),
        USER_ID_ALREADY_EXISTS(ErrorCode.CONFLICT, "이미 존재하는 사용자 ID입니다.", LogLevel.WARN),
        PASSWORD_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "비밀번호는 필수입니다.", LogLevel.ERROR),
        PASSWORD_CONFIRM_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "비밀번호 확인은 필수입니다.", LogLevel.ERROR),
        PASSWORD_CONFIRM_MISMATCH(ErrorCode.BAD_REQUEST, "비밀번호와 비밀번호 확인이 일치하지 않습니다.", LogLevel.ERROR),
        NAME_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "이름은 필수입니다.", LogLevel.ERROR),
        PHONE_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "전화번호는 필수입니다.", LogLevel.ERROR),
        USER_TYPE_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "사용자 유형은 필수입니다.", LogLevel.ERROR),
        PROVIDER_TYPE_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "제공자 유형은 필수입니다.", LogLevel.ERROR),
        LOGIN_ID_OR_PASSWORD_MISMATCH(ErrorCode.BAD_REQUEST, "아이디 또는 비밀번호가 잘못되었습니다.", LogLevel.ERROR),
        USER_STATUS_LOCKED(ErrorCode.FORBIDDEN, "계정이 잠김 상태입니다.", LogLevel.ERROR),
        USER_STATUS_BLOCKED(ErrorCode.FORBIDDEN, "계정이 차단 상태입니다.", LogLevel.ERROR),
        USER_STATUS_DELETED(ErrorCode.FORBIDDEN, "계정이 삭제 상태입니다.", LogLevel.ERROR),
        USER_TYPE_NOT_CHANGE(ErrorCode.FORBIDDEN, "배정받지 않는 사용자만 전환이 가능합니다", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 사업자 관련 에러 타입
     */
    @AllArgsConstructor
    public enum Owner implements IErrorType {
        OWNER_NOT_FOUND(ErrorCode.NOT_FOUND, "사업자 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        BUSINESS_NUMBER_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "사업자 번호는 필수입니다.", LogLevel.ERROR),
        LEVEL_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "등급 ID는 필수입니다.", LogLevel.ERROR),
        STORE_NAME_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "매장명은 필수입니다.", LogLevel.ERROR),
        SAME_LEVEL_CAN_NOT_BE_ASSIGNED(ErrorCode.BAD_REQUEST, "이미 동일한 등급입니다.", LogLevel.ERROR)
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 아동 관련 에러 타입
     */
    @AllArgsConstructor
    public enum Child implements IErrorType {
        CHILD_NOT_FOUND(ErrorCode.NOT_FOUND, "아동 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        CERTIFICATE_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "증명서 파일 경로는 필수입니다.", LogLevel.ERROR),
        NICKNAME_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "닉네임은 필수입니다.", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 소셜 사용자 관련 에러 타입
     */
    @AllArgsConstructor
    public enum UserOAuth implements IErrorType {
        USER_O_AUTH_NOT_FOUND(ErrorCode.NOT_FOUND, "소셜 사용자 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        USER_O_AUTH_LOGIN_FAILED(ErrorCode.UNAUTHORIZED, "소셜 로그인이 실패했습니다.", LogLevel.ERROR),
        UNSUPPORTED_PROVIDER(ErrorCode.BAD_REQUEST, "지원하지 않는 소셜 제공자 입니다.", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 등급 관련 에러 타입
     */
    @AllArgsConstructor
    public enum Level implements IErrorType {
        LEVEL_NOT_FOUND(ErrorCode.NOT_FOUND, "등급 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        NAME_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "등급명은 필수입니다.", LogLevel.ERROR)

        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 가게 관련 에러 타입
     */
    @AllArgsConstructor
    public enum Store implements IErrorType {
        STORE_NOT_FOUND(ErrorCode.NOT_FOUND, "가게 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        STORE_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "가게 ID는 필수입니다.", LogLevel.ERROR),
        STORE_NAME_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "가게 이름은 필수입니다.", LogLevel.ERROR),
        STORE_OWNER_MISMATCH(ErrorCode.BAD_REQUEST, "사업자가 가게의 주인이 아닙니다.", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }
        
        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }
    

    /**
     * 가게 일정 관련 에러 타입
     */
    @AllArgsConstructor
    public enum StoreSchedule implements IErrorType {
        STORE_SCHEDULE_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "가게 일정 ID는 필수입니다.", LogLevel.ERROR),
        STORE_SCHEDULE_NOT_FOUND(ErrorCode.NOT_FOUND, "가게 일정 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        STORE_SCHEDULE_DUPLICATE(ErrorCode.BAD_REQUEST, "중복된 일정이 존재합니다. 같은 날짜와 시간에 일정을 등록할 수 없습니다.", LogLevel.WARN),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }
    /**
     * 태그 관련 에러 타입
     */
    @AllArgsConstructor
    public enum Tag implements IErrorType {
        TAG_ID_MUST_NOT_BE_NULL(ErrorCode.NOT_FOUND, "태그 ID 는 필수입니다.", LogLevel.ERROR),
        TAG_NOT_FOUND(ErrorCode.NOT_FOUND, "태그 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        TAG_ALREADY_EXISTS(ErrorCode.BAD_REQUEST, "이미 존재하는 태그입니다.", LogLevel.WARN),
        TAG_NAME_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "태그 이름은 필수입니다.", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 예약 관련 에러 타입
     */
    @AllArgsConstructor
    public enum Reservation implements IErrorType {
        RESERVATION_NOT_FOUND(ErrorCode.NOT_FOUND, "예약 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        RESERVATION_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "예약 ID는 필수입니다.", LogLevel.ERROR),
        RESERVATION_STORE_SCHEDULE_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "가게 일정 ID는 필수입니다.", LogLevel.ERROR),
        RESERVATION_PEOPLE_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "인원 수는 필수입니다.", LogLevel.ERROR),
        RESERVATION_STATUS_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "예약 상태는 필수입니다.", LogLevel.ERROR),
        RESERVATION_CHILD_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "아동 ID는 필수입니다.", LogLevel.ERROR),
        RESERVATION_CHILD_ID_MISMATCH(ErrorCode.BAD_REQUEST, "예약자와 현재 로그인한 아동 정보가 일치하지 않습니다.", LogLevel.ERROR),
        RESERVATION_ALREADY_EXISTS(ErrorCode.BAD_REQUEST, "이미 예약된 가게 일정입니다.", LogLevel.WARN),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 카테고리 관련 에러 타입
     */
    @AllArgsConstructor
    public enum Category implements IErrorType {
        CATEGORY_NOT_FOUND(ErrorCode.NOT_FOUND, "카테고리 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        CATEGORY_ID_MUST_NOT_BE_NULL(ErrorCode.BAD_REQUEST, "카테고리 ID는 필수입니다.", LogLevel.ERROR),
        CATEGORY_NAME_MUST_NOT_BE_BLANK(ErrorCode.BAD_REQUEST, "카테고리 이름은 필수입니다.", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }

    /**
     * 찜하기 관련 에러 타입
     */
    @AllArgsConstructor
    public enum Favorite implements IErrorType {
        FAVORITE_NOT_FOUND(ErrorCode.NOT_FOUND, "찜하기 정보를 찾을 수 없습니다.", LogLevel.ERROR),
        ;

        private final ErrorCode code;
        private final String message;
        private final LogLevel logLevel;

        @Override
        public ErrorCode getCode() {
            return code;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public LogLevel getLogLevel() {
            return logLevel;
        }
    }
}
