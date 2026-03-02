package com.backend.onharu.interfaces.api.common.dto;

import java.util.List;

import com.backend.onharu.domain.file.dto.FileCommand.ImageMetadata;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * 업로드된 이미지 메타데이터 요청 DTO.
 * <p>
 * Presigned URL로 MinIO 업로드 완료 후 클라이언트가 보관한 정보를 전달할 때 사용합니다.
 * 가게, 리뷰, 프로필 등 이미지 업로드가 필요한 다양한 API에서 공통으로 사용 가능합니다.
 */
@Schema(description = "업로드된 이미지 메타데이터")
public record ImageMetadataRequest(
        @NotBlank(message = "파일 키는 필수입니다.")
        @Schema(description = "S3 객체 키", example = "image/uuid-photo.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String fileKey,

        @NotBlank(message = "파일 경로는 필수입니다.")
        @Schema(description = "이미지 전체 URL", example = "https://minio.example.com/bucket/image/uuid-photo.jpg", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        String filePath,

        @NotNull(message = "표시 순서는 필수입니다.")
        @PositiveOrZero(message = "표시 순서는 0 이상의 숫자여야 합니다.")
        @Schema(description = "표시 순서 (0이 대표 이미지)", example = "0", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
        Integer displayOrder
) {

    /**
     * API 이미지 메타데이터 목록을 도메인 ImageMetadata 목록으로 변환.
     * null 또는 빈 목록이면 null을 반환합니다.
     *
     * @param images API 요청의 이미지 메타데이터 목록
     * @return 도메인 ImageMetadata 목록, 입력이 null 또는 비어 있으면 null
     */
    public static List<ImageMetadata> toImageMetadataList(List<ImageMetadataRequest> images) {
        if (images == null || images.isEmpty()) {
            return null;
        }
        return images.stream()
                .map(img -> new ImageMetadata(img.fileKey(), img.filePath(), img.displayOrder()))
                .toList();
    }
}
