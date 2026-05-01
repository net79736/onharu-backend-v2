package com.backend.onharu.domain.file.dto;

import com.backend.onharu.domain.common.enums.AttachmentType;

/**
 * 파일 메타데이터 등록/삭제용 커맨드
 *
 * 클라이언트가 S3 Presigned URL로 업로드한 뒤, 이 커맨드로 DB에 파일 정보를 등록합니다.
 */
public class FileCommand {

    /**
     * 이미 업로드된 이미지 메타데이터 (Presigned URL 업로드 완료 후 클라이언트가 보관한 정보).
     * Store/Review 등 첨부 등록·교체 시 공통으로 사용합니다.
     * 예시
     * fileKey: image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif
     * filePath: https://localhost:9000/bucket/image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif
     * displayOrder: 0
     */
    public record ImageMetadata(
            String fileKey,
            String filePath,
            Integer displayOrder
    ) {
    }

    /**
     * 파일 메타데이터 등록 커맨드
     *
     * Presigned URL로 업로드 완료 후, 서버에 파일 정보를 저장할 때 사용합니다.
     */
    public record RegisterFileCommand(
            AttachmentType refType,
            Long refId,
            String fileKey,
            String filePath,
            String storedFileName,
            String fileExtension,
            Long fileSize,
            Integer displayOrder
    ) {
    }

    /**
     * 파일 메타데이터 삭제 커맨드 (DB + S3 삭제는 호출부에서 StorageService.deleteFile 호출)
     * id: File 엔티티의 PK (BaseEntity.id)
     */
    public record DeleteFileCommand(
            Long id
    ) {
    }
}
