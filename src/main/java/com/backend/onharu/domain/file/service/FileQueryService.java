package com.backend.onharu.domain.file.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.onharu.domain.file.dto.FileQuery.GetByFileKeyQuery;
import com.backend.onharu.domain.file.dto.FileQuery.GetByIdQuery;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefQuery;
import com.backend.onharu.domain.file.dto.FileQuery.ListByRefsQuery;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.repository.FileRepository;

import lombok.RequiredArgsConstructor;

/**
 * 파일 메타데이터 조회 서비스
 */
@Service
@RequiredArgsConstructor
public class FileQueryService {

    private final FileRepository fileRepository;

    /**
     * 특정 게시물(상점·리뷰 등)에 첨부된 파일 목록을 표시 순서대로 반환합니다.
     * 
     * @param query 조회 조건 (refType: STORE, REVIEW, refId: 상점 ID 또는 리뷰 ID)
     * @return 파일 목록
     */
    public List<File> listByRef(ListByRefQuery query) {
        return fileRepository.findByRefTypeAndRefIdOrderByDisplayOrderAsc(query.refType(), query.refId());
    }

    /**
     * 파일 ID로 단건 조회. 없으면 Repository에서 FILE_NOT_FOUND 예외.
     */
    public File getById(GetByIdQuery query) {
        return fileRepository.getById(query.id());
    }

    /**
     * 파일 키(S3 객체 키)로 단건 조회. 없으면 Repository에서 FILE_NOT_FOUND 예외.
     */
    public File getByFileKey(GetByFileKeyQuery query) {
        return fileRepository.getByFileKey(query.fileKey());
    }

    /**
     * 여러 게시물에 첨부된 파일 목록을 배치로 조회합니다.
     * 
     * @param query 조회 조건 (refType: STORE, REVIEW, refIds: 게시물 ID 목록)
     * @return 파일 목록 (표시 순서대로 정렬)
     */
    public List<File> listByRefs(ListByRefsQuery query) {
        if (query.refIds() == null || query.refIds().isEmpty()) {
            return List.of();
        }
        return fileRepository.findByRefTypeAndRefIdInOrderByDisplayOrderAsc(query.refType(), query.refIds());
    }
}