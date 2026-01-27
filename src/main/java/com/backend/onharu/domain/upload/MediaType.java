package com.backend.onharu.domain.upload;

/**
 * 미디어 타입 열거형
 * 
 * S3에 업로드 가능한 파일의 MIME 타입을 정의합니다.
 * 각 타입은 표준 MIME 타입 문자열을 가지고 있습니다.
 * 
 * 지원 형식:
 * - 이미지: PNG, JPEG, WEBP, HEIC
 * 
 * 사용 예시:
 * - 파일 업로드 시 MIME 타입 검증
 * - Content-Type 헤더 설정
 */
public enum MediaType {
    IMAGE_PNG("image/png"),
    IMAGE_JPG("image/jpg"),
    IMAGE_JPEG("image/jpeg"),
    IMAGE_WEBP("image/webp"),
    IMAGE_HEIC("image/heic");

    private final String value;

    MediaType(String value) {
        this.value = value;
    }

    /**
     * MIME 타입 문자열 반환
     * 
     * @return MIME 타입 (예: "image/png")
     */
    public String getValue() {
        return value;
    }

    /**
     * MIME 타입 문자열로 MediaType 찾기
     * 
     * @param value MIME 타입 문자열
     * @return 일치하는 MediaType, 없으면 null
     */
    public static MediaType fromValue(String value) {
        for (MediaType type : MediaType.values()) {
            if (type.value.equals(value)) {
                return type;
            }
        }
        return null;
    }

    /**
     * 유효한 MIME 타입인지 확인
     * 
     * @param value MIME 타입 문자열
     * @return 유효하면 true, 아니면 false
     */
    public static boolean isValid(String value) {
        return fromValue(value) != null;
    }
}


