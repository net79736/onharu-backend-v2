package com.backend.onharu.domain.file.dto;

import com.backend.onharu.domain.common.enums.AttachmentType;

/**
 * 파일 조회용 쿼리
 */
public class FileQuery {

    /**
     * 특정 게시물에 첨부된 파일 목록 조회
     */
    public record ListByRefQuery(
            AttachmentType refType,
            Long refId
    ) {
    }

    /**
     * 파일 키로 단건 조회
     */
    public record GetByFileKeyQuery(
            String fileKey
    ) {
    }

    /**
     * 파일 ID로 단건 조회 (BaseEntity.id = File PK)
     */
    public record GetByIdQuery(
            Long id
    ) {
    }
}
