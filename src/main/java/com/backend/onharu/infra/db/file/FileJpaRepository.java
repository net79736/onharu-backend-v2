package com.backend.onharu.infra.db.file;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.model.File;

/**
 * 파일 JPA Repository
 */
public interface FileJpaRepository extends JpaRepository<File, Long> {

    /**
     * 파일 키로 단건 조회.
     * @param fileKey 파일 키
     * @return 파일
     */
    Optional<File> findByFileKey(String fileKey);

    /**
     * 게시물별 파일 목록 조회.
     * @param refType 게시물 유형 (STORE, REVIEW)
     * @param refId 게시물 ID (상점 ID 또는 리뷰 ID)
     * @return 파일 목록
     */
    List<File> findByRefTypeAndRefIdOrderByDisplayOrderAsc(AttachmentType refType, Long refId);

    /**
     * 특정 게시물에 첨부된 파일 메타데이터를 모두 삭제합니다.
     */
    @Modifying
    @Query("DELETE FROM File f WHERE f.refType = :refType AND f.refId = :refId")
    void deleteByRefTypeAndRefId(@Param("refType") AttachmentType refType, @Param("refId") Long refId);
}
