package com.backend.onharu.domain.file.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.backend.onharu.domain.common.base.BaseEntity;
import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.dto.FileCommand.ImageMetadata;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 파일 메타데이터 엔티티 (S3 등에 저장된 파일 정보)
 *
 * 실제 파일은 S3/MinIO에 저장되고, 이 엔티티는 파일 키·경로·크기 등 메타데이터만 보관합니다.
 * 하나의 게시물(상점, 리뷰 등)에 여러 파일을 연결할 수 있도록 refType + refId로 소속을 구분합니다.
 *
 * 스키마 참고: FILE_NO(id), FILE_KEY(fileKey),
 * MOD_FILE_NM(storedFileName), FILE_PATH(filePath), FILE_EXT(fileExtension), FILE_SIZE(fileSize)
 */
@Entity
@Table(
    name = "files",
    indexes = {
        @Index(name = "idx_files_ref", columnList = "ref_type, ref_id"),
        @Index(name = "idx_files_file_key", columnList = "file_key", unique = true)
    }
)
@EntityListeners(AuditingEntityListener.class)
@Getter
@NoArgsConstructor
public class File extends BaseEntity {

    /**
     * S3 객체 키 (버킷 내 고유 경로). 이 키로 S3에서 객체를 조회·삭제합니다.
     * 예시: image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif
     */
    @Column(name = "file_key", nullable = false, unique = true, length = 255)
    private String fileKey;

    /**
     * 저장 시 사용하는 변환된 파일명 (중복·보안을 위해 UUID 등 사용)
     */
    @Column(name = "stored_file_name", nullable = false, length = 255)
    private String storedFileName;

    /**
     * 파일 경로 또는 다운로드 URL (S3 경로)
     */
    @Column(name = "file_path", nullable = false, length = 500)
    private String filePath;

    /**
     * 파일 확장자 (예: jpg, png)
     */
    @Column(name = "file_extension", length = 20)
    private String fileExtension;

    /**
     * 파일 크기 (바이트)
     */
    @Column(name = "file_size")
    private Long fileSize;

    /**
     * 첨부된 게시물 유형 (STORE, REVIEW 등)
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false, length = 20)
    private AttachmentType refType;

    /**
     * 첨부된 게시물 ID (Store id 또는 Review id)
     */
    @Column(name = "ref_id", nullable = false)
    private Long refId;

    /**
     * 같은 게시물 내 표시 순서 (0이 대표 이미지 등)
     */
    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Builder
    public File(String fileKey, String storedFileName, String filePath,
                String fileExtension, Long fileSize, AttachmentType refType, Long refId, Integer displayOrder) {
        this.fileKey = fileKey;
        this.storedFileName = storedFileName;
        this.filePath = filePath;
        this.fileExtension = fileExtension;
        this.fileSize = fileSize;
        this.refType = refType;
        this.refId = refId;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    // File 엔티티 내부에 팩토리 메서드나 정적 메서드 생성
    public static File create(ImageMetadata img, AttachmentType refType, Long refId, int defaultOrder) {
        String fileKey = img.fileKey(); // 파일 키 추출 (예시: image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif)
        String fileName = extractFileName(fileKey); // 파일 이름 추출 (예시: 2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif)
        
        return File.builder()
                .fileKey(fileKey)
                .filePath(img.filePath()) // 파일 경로 추출 (예시: https://minio.example.com/bucket/image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif)
                .storedFileName(fileName) // 파일 이름 추출 (예시: 2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif)
                .fileExtension(extractExtension(fileName)) // 파일 확장자 추출
                .refType(refType)
                .refId(refId)
                .displayOrder(img.displayOrder() != null ? img.displayOrder() : defaultOrder)
                .build();
    }

    /**
     * 파일 이름 추출
     * 예시: "image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif" -> "2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif"
     * 
     * @param fileKey 파일 키
     * @return
     */
    private static String extractFileName(String fileKey) {
        return fileKey.contains("/") ? fileKey.substring(fileKey.lastIndexOf("/") + 1) : fileKey; // 파일 이름 추출
    }

    private static String extractExtension(String fileName) {
        int dotIndex = fileName.lastIndexOf(".");
        return (dotIndex == -1) ? null : fileName.substring(dotIndex + 1);
    }
}
