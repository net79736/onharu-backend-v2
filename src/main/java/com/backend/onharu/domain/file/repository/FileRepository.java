package com.backend.onharu.domain.file.repository;

import java.util.List;

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.model.File;

/**
 * 파일 메타데이터 Repository 인터페이스
 *
 * S3에 저장된 파일의 메타데이터(File 엔티티)를 저장·조회·삭제합니다.
 * NOT_FOUND 등 조회 실패 시 예외는 Repository 구현체에서 던집니다.
 */
public interface FileRepository {

    File save(File file);

    List<File> saveAll(List<File> files);

    void delete(File file);

    /**
     * 파일 ID로 단건 조회. 없으면 FILE_NOT_FOUND 예외.
     */
    File getById(Long id);

    /**
     * 파일 키(S3 객체 키)로 단건 조회. 없으면 FILE_NOT_FOUND 예외.
     * 
     * @param fileKey 파일 키 (fileKey: image/2ef66e61-4470-40df-8ce2-affbb6466d8c-photo.gif)
     * @return 파일
     */
    File getByFileKey(String fileKey);

    /**
     * 특정 게시물(상점·리뷰 등)에 첨부된 파일 목록을 표시 순서대로 조회합니다.
     */
    List<File> findByRefTypeAndRefIdOrderByDisplayOrderAsc(AttachmentType refType, Long refId);

    /**
     * 특정 게시물(상점·리뷰 등)에 첨부된 파일 메타데이터를 모두 삭제합니다.
     * (수정 시 이미지 목록 교체 시 사용)
     */
    void deleteByRefTypeAndRefId(AttachmentType refType, Long refId);
}
