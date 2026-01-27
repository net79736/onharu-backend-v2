package com.backend.onharu.domain.upload.service;

import com.backend.onharu.interfaces.api.dto.S3ControllerDto.S3FileUploadResponse;

/**
 * 파일 저장소 서비스 인터페이스
 * 
 * S3, MinIO 등 다양한 저장소 구현체를 추상화합니다.
 * 실제 구현체는 프로파일(dev, prod)에 따라 선택됩니다.
 * 
 * 구현체:
 * - S3Service: AWS S3 (prod 프로파일)
 * - MinioService: MinIO (dev, test 프로파일)
 */
public interface StorageService {
    
    /**
     * Presigned URL 생성
     * 
     * 클라이언트가 파일을 직접 업로드할 수 있도록
     * 서명된 URL을 생성합니다. URL은 일정 시간(보통 5분) 후 만료됩니다.
     * 
     * @param fileName 업로드할 파일의 이름
     * @param contentType 파일의 MIME 타입 (예: "image/jpeg")
     * @return Presigned URL과 다운로드 URL을 포함한 응답
     */
    S3FileUploadResponse generatePresignedUrl(String fileName, String contentType);

    /**
     * 파일 삭제
     * 
     * 저장소에서 파일을 삭제합니다.
     * 
     * @param fileName 삭제할 파일의 전체 경로 (예: "image/uuid-photo.jpg")
     * @return 파일 삭제 성공 여부
     */
    boolean deleteFile(String fileName);

    /**
     * 파일 다운로드
     * 
     * 저장소에서 파일을 다운로드하여 바이트 배열로 반환합니다.
     * 
     * @param fileName 다운로드할 파일의 전체 경로 (예: "image/uuid-photo.jpg")
     * @return 파일의 바이트 배열
     */
    byte[] downloadFile(String fileName);

    /**
     * 파일의 Content-Type 조회
     * 
     * 저장소에 저장된 파일의 MIME 타입을 조회합니다.
     * 
     * @param fileName 조회할 파일의 전체 경로 (예: "image/uuid-photo.jpg")
     * @return 파일의 MIME 타입 (예: "image/jpeg")
     */
    String getContentType(String fileName);
}

