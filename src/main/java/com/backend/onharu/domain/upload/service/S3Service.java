package com.backend.onharu.domain.upload.service;

import java.time.Duration;
import java.util.UUID;

import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.upload.MediaType;
import com.backend.onharu.interfaces.api.dto.S3ControllerDto.S3FileUploadResponse;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

/**
 * S3 파일 저장소 서비스
 * 
 * S3를 사용하여 파일을 저장하고 관리합니다.
 * 운영 환경(prod 프로파일)에서 사용됩니다.
 * 
 * 주요 기능:
 * 1. Presigned URL 생성: 클라이언트가 직접 S3에 업로드
 * 2. 파일 삭제: S3 객체 삭제
 * 3. MIME 타입 검증: 허용된 파일 형식만 업로드
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Profile({"prod"})
public class S3Service implements StorageService {

    private static final String S3_KEY_DELIMITER = "/";

    private final S3Client awsS3Client;
    private final S3Presigner awsS3Presigner;
    
    @Value("${aws.s3.bucket}")
    private String bucket;
    
    @Value("${aws.s3.url}")
    private String awsS3Url;  // 백엔드가 AWS S3에 연결할 때 사용하는 AWS S3 URL 주소
    
    /**
     * Presigned URL 생성
     * 
     * @param fileName 업로드할 파일의 이름
     * @param contentType 파일의 MIME 타입
     * @return Presigned URL과 다운로드 URL
     */
    @Override
    public S3FileUploadResponse generatePresignedUrl(String fileName, String contentType) {
        // 미디어 타입 & 파일명 검사
        verifyMimeType(contentType, fileName);

        // 폴더 (image or video) 설정
        String feature = contentType.split("/")[0].equalsIgnoreCase("image") ? "image" : "video";

        // 파일명은 고유하도록 UUID 설정
        String uniqueFileName = feature + S3_KEY_DELIMITER + UUID.randomUUID() + "-" + fileName;

        try {
            // Presigned URL 요청 생성
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(uniqueFileName)
                    .contentType(contentType)
                    .build();

            Duration expiration = Duration.ofMinutes(5);

            // PresignedPutObjectRequest 생성
            PresignedPutObjectRequest presignedPutObjectRequest = awsS3Presigner.presignPutObject(
                    presignRequest -> presignRequest
                            .putObjectRequest(putObjectRequest)
                            .signatureDuration(expiration)
            );

            String downloadUrl = awsS3Url + "/" + bucket + "/" + uniqueFileName;

            log.info("S3Service downloadUrl: {}", downloadUrl);

            S3FileUploadResponse response = new S3FileUploadResponse(
                    presignedPutObjectRequest.url().toString(),
                    downloadUrl
            );

            log.info("S3 Presigned URL 생성 완료 - fileName: {}, uniqueFileName: {}", fileName, uniqueFileName);
            
            return response;

        } catch (Exception e) {
            log.error("S3 Presigned URL 생성 실패: {}", e.getMessage(), e);
            throw new CoreException(ErrorType.FileOperation.FILE_UPLOAD_ERROR);
        }
    }

    /**
     * 파일 삭제
     * 
     * @param fileName 삭제할 파일의 전체 경로
     * @return 삭제 성공 여부
     */
    @Override
    public boolean deleteFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("파일 삭제 요청이 들어왔지만, fileName이 null 또는 빈 문자열입니다.");
            return false;
        }

        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            awsS3Client.deleteObject(deleteObjectRequest);
            log.info("S3 파일 삭제 성공 - fileName: {}", fileName);
            return true;
        } catch (S3Exception e) {
            log.error("S3 파일 삭제 실패: {}", e.awsErrorDetails().errorMessage(), e);
            throw new CoreException(ErrorType.FileOperation.FILE_DELETE_ERROR);
        }
    }

    /**
     * 파일 다운로드
     * 
     * @param fileName 다운로드할 파일의 전체 경로 (예: "image/uuid-photo.jpg")
     * @return 파일의 바이트 배열
     */
    @Override
    public byte[] downloadFile(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("파일 다운로드 요청이 들어왔지만, fileName이 null 또는 빈 문자열입니다.");
            throw new CoreException(ErrorType.FileOperation.FILE_NOT_FOUND);
        }

        try {
            log.info("S3Service downloadFile: fileName={}", fileName);
            log.info("S3Service downloadFile: bucket={}", bucket);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            ResponseBytes<GetObjectResponse> responseBytes = awsS3Client.getObjectAsBytes(getObjectRequest);
            byte[] fileBytes = responseBytes.asByteArray();
            
            log.info("S3 파일 다운로드 성공 - fileName: {}, size: {} bytes", fileName, fileBytes.length);
            return fileBytes;
        } catch (S3Exception e) {
            log.error("S3 파일 다운로드 실패: {}", e.awsErrorDetails().errorMessage(), e);
            if (e.statusCode() == 404) {
                throw new CoreException(ErrorType.FileOperation.FILE_NOT_FOUND);
            }
            throw new CoreException(ErrorType.FileOperation.FILE_DOWNLOAD_ERROR);
        } catch (Exception e) {
            log.error("S3 파일 다운로드 중 예외 발생: {}", e.getMessage(), e);
            throw new CoreException(ErrorType.FileOperation.FILE_DOWNLOAD_ERROR);
        }
    }

    /**
     * 파일의 Content-Type 조회
     * 
     * S3에 저장된 파일의 MIME 타입을 조회합니다.
     * 파일이 존재하지 않으면 기본값으로 "application/octet-stream"을 반환합니다.
     * 
     * @param fileName 조회할 파일의 전체 경로 (예: "image/uuid-photo.jpg")
     * @return 파일의 MIME 타입 (예: "image/jpeg")
     */
    @Override
    public String getContentType(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            log.warn("Content-Type 조회 요청이 들어왔지만, fileName이 null 또는 빈 문자열입니다.");
            return "application/octet-stream";
        }

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileName)
                    .build();

            HeadObjectResponse headObjectResponse = awsS3Client.headObject(headObjectRequest);
            String contentType = headObjectResponse.contentType();
            
            // Content-Type이 없으면 기본값 반환
            if (contentType == null || contentType.isBlank()) {
                contentType = "application/octet-stream";
            }
            
            log.debug("S3 파일 Content-Type 조회 - fileName: {}, contentType: {}", fileName, contentType);
            return contentType;
        } catch (S3Exception e) {
            log.warn("S3 파일 Content-Type 조회 실패: {}, 기본값 반환", e.awsErrorDetails().errorMessage());
            // 파일이 존재하지 않거나 조회 실패 시 기본값 반환
            return "application/octet-stream";
        } catch (Exception e) {
            log.warn("Content-Type 조회 중 예외 발생: {}, 기본값 반환", e.getMessage());
            return "application/octet-stream";
        }
    }

    /**
     * Presigned GET URL 생성
     *
     * 프라이빗 S3 버킷의 파일에 임시 접근할 수 있는 서명된 GET URL을 생성합니다.
     *
     * @param fileKey 파일 키 (예: "image/uuid-photo.jpg")
     * @return 서명된 GET URL (3분 유효)
     */
    @Override
    public String generatePresignedGetUrl(String fileKey) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(fileKey)
                    .build();

            PresignedGetObjectRequest presignedRequest = awsS3Presigner.presignGetObject(
                    presignRequest -> presignRequest
                            .getObjectRequest(getObjectRequest)
                            .signatureDuration(Duration.ofMinutes(3))
            );

            log.info("S3 Presigned GET URL 생성 완료 - fileKey: {}", fileKey);
            return presignedRequest.url().toString();
        } catch (S3Exception e) {
            log.error("S3 Presigned GET URL 생성 실패: {}", e.awsErrorDetails().errorMessage(), e);
            throw new CoreException(ErrorType.FileOperation.FILE_DOWNLOAD_ERROR);
        } catch (Exception e) {
            log.error("S3 Presigned GET URL 생성 중 예외 발생: {}", e.getMessage(), e);
            throw new CoreException(ErrorType.FileOperation.FILE_DOWNLOAD_ERROR);
        }
    }

    /**
     * MIME 타입 & 확장자 검사
     * 
     * 파일의 MIME 타입과 확장자가 일치하고,
     * 허용된 형식인지 검증합니다.
     * 
     * @param contentType MIME 타입
     * @param fileName 파일명
     * @throws ValidationException 유효하지 않은 경우
     */
    private void verifyMimeType(String contentType, String fileName) {
        log.info("verifyMimeType: contentType={}, fileName={}", contentType, fileName);
        // 파일 확장자 추출
        String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();
        log.info("fileExtension={}", fileExtension);
        log.info("isValidMimeType={}", isValidMimeType(contentType, fileExtension));
        if (!isValidMimeType(contentType, fileExtension)) {
            log.info("isValidMimeType=false");
            throw new CoreException(ErrorType.FileOperation.FILE_UPLOAD_INVALID_MIME_TYPE_ERROR);
        }
    }

    /**
     * 유효한 MIME 타입 및 확장자 체크
     * 
     * MediaType enum에 정의된 형식과 일치하는지 확인합니다.
     * 
     * @param contentType MIME 타입
     * @param fileExtension 파일 확장자
     * @return 유효하면 true, 아니면 false
     */
    private boolean isValidMimeType(String contentType, String fileExtension) {
        log.info("isValidMimeType: contentType={}, fileExtension={}", contentType, fileExtension);
        for (MediaType type : MediaType.values()) {
            if (type.getValue().equals(contentType)) {
                // MIME 타입에서 / 뒤에 있는 확장자 부분을 추출
                String mimeExtension = contentType.split("/")[1].toLowerCase();
                
                // jpeg는 jpg로 매칭 (jpeg와 jpg는 동일한 형식)
                if (mimeExtension.equals("jpeg")) {
                    mimeExtension = "jpg";
                }

                // 파일 확장자도 jpeg를 jpg로 변환 (jpeg와 jpg는 동일한 형식)
                String normalizedExtension = fileExtension.toLowerCase();
                if (normalizedExtension.equals("jpeg")) {
                    normalizedExtension = "jpg";
                }

                // 정규화된 확장자와 비교
                if (mimeExtension.equals(normalizedExtension)) {
                    return true;
                }
            }
        }
        return false;
    }
}

