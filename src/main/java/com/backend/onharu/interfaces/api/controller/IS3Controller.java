package com.backend.onharu.interfaces.api.controller;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ResponseEntity;

import com.backend.onharu.interfaces.api.common.dto.ErrorResponse;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.S3ControllerDto.S3FileUploadResponse;
import com.backend.onharu.interfaces.api.dto.S3ControllerDto.S3UploadRequest;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * S3 파일 업로드 컨트롤러 인터페이스
 *
 * S3/MinIO를 사용한 파일 업로드 관련 API를 제공합니다.
 * Presigned URL 방식을 사용하여 클라이언트가 직접 파일을 업로드합니다.
 *
 * 엔드포인트:
 * - GET /api/upload: Presigned URL 생성
 * - DELETE /api/upload/delete: 파일 삭제
 *
 * 워크플로우:
 * 1. 클라이언트가 fileName, contentType으로 Presigned URL 요청
 * 2. 서버가 Presigned URL과 다운로드 URL 반환
 * 3. 클라이언트가 Presigned URL로 PUT 요청하여 파일 업로드
 * 4. 업로드 완료 후 다운로드 URL로 파일 접근 가능
 *
 * 프로파일별 동작:
 * - prod: AWS S3 사용
 * - dev/test: MinIO 사용
 */
@Tag(name = "S3 관련 API", description = "파일 업로드를 위한 S3 관련 API")
public interface IS3Controller {

    /**
     * Presigned URL 생성 API
     *
     * 클라이언트가 파일을 업로드하기 위한 Presigned URL을 생성합니다.
     * URL은 5분 동안 유효하며, 만료 후에는 사용할 수 없습니다.
     *
     * 요청 예시:
     * GET /api/upload?fileName=profile.jpg&contentType=image/jpeg
     *
     * 응답 예시:
     * {
     *   "status": "SUCCESS",
     *   "message": "Presigned URL 생성 성공",
     *   "data": {
     *     "presignedUrl": "https://s3.amazonaws.com/bucket/...",
     *     "downloadUrl": "https://s3.amazonaws.com/bucket/image/uuid-profile.jpg"
     *   }
     * }
     *
     * @param request 파일명과 Content-Type 포함
     * @return Presigned URL과 다운로드 URL
     */
    @Operation(
            summary = "Presigned URL 생성",
            description = "파일 업로드를 위한 Presigned URL을 생성합니다. URL은 5분 동안 유효합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Presigned URL 생성 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 형식 (유효하지 않은 MIME 타입 또는 파일명)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "S3/MinIO 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
    })
    ResponseEntity<ResponseDTO<S3FileUploadResponse>> generatePresignedUrl(S3UploadRequest request);

    /**
     * 파일 삭제 API
     *
     * S3/MinIO에 저장된 파일을 삭제합니다.
     *
     * 요청 예시:
     * DELETE /api/upload/delete?fileName=image/uuid-profile.jpg
     *
     * 응답 예시:
     * {
     *   "status": "SUCCESS",
     *   "message": "파일 삭제 성공",
     *   "data": null
     * }
     *
     * @param fileName 삭제할 파일의 전체 경로 (예: image/8be43abc-455f-4c4a-9458-87f6a20d3008-photo.jpeg)
     * @return 성공 메시지
     */
    @Operation(
            summary = "파일 삭제",
            description = "S3/MinIO에 업로드된 파일을 삭제합니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "파일 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 형식 (파일명 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "S3/MinIO 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
    })
    ResponseEntity<ResponseDTO<String>> deleteFile(String fileName);

    /**
     * 파일 다운로드 API
     *
     * S3/MinIO에 저장된 파일을 다운로드합니다.
     * 파일은 바이트 배열로 반환되며, Content-Type 헤더에 MIME 타입이 설정됩니다.
     *
     * 요청 예시:
     * GET /api/upload/download?fileName=image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif
     *
     * 응답:
     * - Content-Type: 파일의 MIME 타입 (예: image/gif)
     * - Body: 파일의 바이트 배열
     *
     * @param fileName 다운로드할 파일의 전체 경로 (예: image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif)
     * @return 파일의 바이트 배열과 Content-Type 헤더
     */
    @Operation(
            summary = "파일 다운로드",
            description = "S3/MinIO에 저장된 파일을 다운로드합니다. 파일은 바이트 배열로 반환됩니다."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "파일 다운로드 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 형식 (파일명 누락)",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "파일을 찾을 수 없음",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "S3/MinIO 서버 오류",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
    })
    ResponseEntity<ByteArrayResource> downloadFile(String fileName);
}
