package com.backend.onharu.interfaces.api.common.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 공통 API 응답 DTO
 * 
 * @param <T> 응답 데이터 타입
 */
@Data
@NoArgsConstructor
public class ResponseDTO<T> {
    
    /**
     * 성공 여부
     */
    private boolean success;
    
    /**
     * 응답 데이터
     */
    private T data;

    /**
     * 성공 응답을 생성하는 생성자
     * 
     * @param data 응답 데이터
     */
    public ResponseDTO(T data) {
        this.success = true;
        this.data = data;
    }

    /**
     * 성공 응답을 생성하는 메서드
     * 
     * @param data 응답 데이터
     * @return ResponseDTO 인스턴스
     */
    public static <T> ResponseDTO<T> success(T data) {
        return new ResponseDTO<>(data);
    }
}
