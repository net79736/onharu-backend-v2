package com.backend.onharu.interfaces.api.controller.impl;

import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.backend.onharu.domain.upload.service.StorageService;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.IS3Controller;
import com.backend.onharu.interfaces.api.dto.S3ControllerDto.S3FileUploadResponse;
import com.backend.onharu.interfaces.api.dto.S3ControllerDto.S3UploadRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
public class S3ControllerImpl implements IS3Controller {

    private final StorageService storageService;

    @Override
    @GetMapping
    public ResponseEntity<ResponseDTO<S3FileUploadResponse>> generatePresignedUrl(
            @ModelAttribute S3UploadRequest request) {

        log.info("Presigned URL 생성 요청 - fileName: {}, contentType: {}", 
                request.fileName(), request.contentType());

        // Presigned URL 생성
        S3FileUploadResponse response = storageService.generatePresignedUrl(
                request.fileName(),
                request.contentType()
        );

        return ResponseEntity.ok(
                new ResponseDTO<>(response)
        );
    }

    @Override
    @DeleteMapping("/delete")
    public ResponseEntity<ResponseDTO<String>> deleteFile(@RequestParam String fileName) {

        log.info("파일 삭제 요청 - fileName: {}", fileName);

        // S3/MinIO 객체 삭제
        storageService.deleteFile(fileName);
        
        return ResponseEntity.ok(
                new ResponseDTO<>(null)
        );
    }

    /**
     * 파일 다운로드
     * 
     * GET /api/upload/download?fileName=image/uuid-filename.ext
     * 
     * S3/MinIO에 저장된 파일을 다운로드하여 바이트 배열로 반환합니다.
     * Content-Type 헤더에 파일의 MIME 타입이 자동으로 설정됩니다.
     * 
     * @param fileName 다운로드할 파일의 전체 경로 (예: image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif)
     * @return 파일의 바이트 배열과 Content-Type 헤더
     */
    @Override
    @GetMapping("/download")
    public ResponseEntity<ByteArrayResource> downloadFile(@RequestParam String fileName) {
        
        log.info("파일 다운로드 요청 - fileName: {}", fileName);

        // 파일명 유효성 검사
        if (fileName == null || fileName.isBlank()) {
            log.warn("파일 다운로드 요청이 들어왔지만, fileName이 null 또는 빈 문자열입니다.");
            return ResponseEntity.badRequest().build();
        }

        try {
            // S3/MinIO에서 파일 다운로드
            byte[] fileBytes = storageService.downloadFile(fileName);
            
            // 파일의 Content-Type 조회
            String contentType = storageService.getContentType(fileName);
            
            log.info("파일 다운로드 성공 - fileName: {}, contentType: {}", fileName, contentType);

            // Content-Type이 없거나 기본값인 경우 파일 확장자로 추론 시도
            if (contentType == null || contentType.equals("application/octet-stream")) {
                contentType = inferContentTypeFromFileName(fileName);
            }
            
            // 파일명에서 다운로드 파일명 추출 (UUID 제거)
            String downloadFileName = extractDownloadFileName(fileName);
            log.info("파일 다운로드 성공 - downloadFileName: {}", downloadFileName);

            log.info("파일 다운로드 성공 - fileName: {}, contentType: {}, size: {} bytes", 
                    fileName, contentType, fileBytes.length);

            // ByteArrayResource 변환하여 스트리밍 방식으로 전송
            ByteArrayResource resource = new ByteArrayResource(fileBytes);

            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(contentType));
            headers.setContentLength(fileBytes.length);
            headers.add(HttpHeaders.CONTENT_DISPOSITION, 
                    "attachment; filename=\"" + downloadFileName + "\"");

            return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(resource);

        } catch (RuntimeException e) {
            log.error("파일 다운로드 실패 - fileName: {}, error: {}", fileName, e.getMessage(), e);
            
            // 파일을 찾을 수 없는 경우 404 반환
            if (e.getMessage() != null && e.getMessage().contains("찾을 수 없습니다")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
            
            // 기타 오류는 500 반환
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch (Exception e) {
            log.error("파일 다운로드 중 예외 발생 - fileName: {}, error: {}", fileName, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * 파일명에서 Content-Type 추론
     * 
     * 파일 확장자를 기반으로 MIME 타입을 추론합니다.
     * 
     * @param fileName 파일명 (예: image/uuid-photo.jpg)
     * @return 추론된 MIME 타입 (예: image/jpeg)
     */
    private String inferContentTypeFromFileName(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        
        // 이미지 파일
        if (lowerFileName.endsWith(".jpg") || lowerFileName.endsWith(".jpeg")) {
            return "image/jpeg";
        } else if (lowerFileName.endsWith(".png")) {
            return "image/png";
        } else if (lowerFileName.endsWith(".gif")) {
            return "image/gif";
        } else if (lowerFileName.endsWith(".webp")) {
            return "image/webp";
        } else if (lowerFileName.endsWith(".heic")) {
            return "image/heic";
        }
        // 비디오 파일
        else if (lowerFileName.endsWith(".mp4")) {
            return "video/mp4";
        } else if (lowerFileName.endsWith(".mov")) {
            return "video/quicktime";
        } else if (lowerFileName.endsWith(".webm")) {
            return "video/webm";
        }
        // 문서 파일
        else if (lowerFileName.endsWith(".pdf")) {
            return "application/pdf";
        } else if (lowerFileName.endsWith(".txt")) {
            return "text/plain";
        } else if (lowerFileName.endsWith(".doc")) {
            return "application/msword";
        } else if (lowerFileName.endsWith(".docx")) {
            return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        } else if (lowerFileName.endsWith(".xls")) {
            return "application/vnd.ms-excel";
        } else if (lowerFileName.endsWith(".xlsx")) {
            return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        }
        
        // 기본값
        return "application/octet-stream";
    }

    /**
     * 파일명에서 다운로드 파일명 추출
     * 
     * UUID가 포함된 파일명에서 원본 파일명 부분만 추출합니다.
     * 예: "image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif" -> "photo.gif"
     * 
     * @param fileName 전체 파일 경로
     * @return 다운로드 파일명
     */
    private String extractDownloadFileName(String fileName) {
        // 파일명에서 마지막 "/" 이후 부분 추출
        int lastSlashIndex = fileName.lastIndexOf("/");
        String fileNameOnly = lastSlashIndex >= 0 ? fileName.substring(lastSlashIndex + 1) : fileName;
        
        // UUID 패턴 제거 (UUID는 8-4-4-4-12 형식)
        // 예: "2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif" -> "photo.gif"
        String uuidPattern = "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}-";
        String cleanedFileName = fileNameOnly.replaceFirst(uuidPattern, "");
        
        // UUID 제거 후에도 파일명이 비어있으면 원본 파일명 반환
        if (cleanedFileName == null || cleanedFileName.isBlank()) {
            return fileNameOnly;
        }
        
        return cleanedFileName;
    }
}

