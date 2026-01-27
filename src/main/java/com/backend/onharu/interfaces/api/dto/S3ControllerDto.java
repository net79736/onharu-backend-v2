package com.backend.onharu.interfaces.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class S3ControllerDto {

    /**
     * S3 파일 업로드 요청 DTO
     * 
     * Presigned URL 생성을 위한 요청 정보를 담습니다.
     * 클라이언트는 파일명과 Content-Type을 전달하여
     * 서버로부터 업로드용 Presigned URL을 받습니다.
     */
    public record S3UploadRequest(
            @Schema(description = "업로드할 파일명", example = "profile.jpeg", requiredMode = Schema.RequiredMode.REQUIRED)
            String fileName,
            
            @Schema(description = "파일의 MIME 타입", example = "image/jpeg", requiredMode = Schema.RequiredMode.REQUIRED)
            String contentType
    ) {
    }

    /**
     * S3 파일 업로드 응답 DTO
     * 
     * Presigned URL과 다운로드 URL을 클라이언트에게 전달합니다.
     */
    public record S3FileUploadResponse(
            @Schema(description = "파일 업로드용 Presigned URL (PUT 요청)", 
                    example = "https://s3.amazonaws.com/bucket/file?X-Amz-Algorithm=...")
            String presignedUrl,
            
            @Schema(description = "파일 다운로드 URL (GET 요청)", 
                    example = "https://s3.amazonaws.com/bucket/image/uuid-file.jpg")
            String downloadUrl
    ) {
    }
}
