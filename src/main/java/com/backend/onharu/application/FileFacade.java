package com.backend.onharu.application;

import java.util.List;
import java.util.stream.IntStream;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.repository.FileRepository;
import com.backend.onharu.domain.file.dto.FileCommand.ImageMetadata;

import lombok.RequiredArgsConstructor;

/**
 * 파일(첨부) 메타데이터 조회·등록 파사드
 *
 * 상점/리뷰 등 게시물에 연결된 파일 메타데이터를 일괄 등록하는 역할을 담당합니다.
 */
@Component
@RequiredArgsConstructor
public class FileFacade {

    private final FileRepository fileRepository;

    /**
     * 이미 업로드된 파일 메타데이터를 지정한 게시물(refType, refId)에 일괄 등록합니다.
     * 이미지 먼저 업로드 → 게시물 저장 시 한 번에 연결하는 플로우에서 사용합니다.
     *
     * @param refType 첨부 대상 유형 (STORE, REVIEW)
     * @param refId   첨부 대상 ID (상점 ID 또는 리뷰 ID)
     * @param images  이미 업로드된 파일 정보 (fileKey, filePath, displayOrder)
     */
    @Transactional
    public void registerFiles(AttachmentType refType, Long refId, List<ImageMetadata> images) {
        if (images == null || images.isEmpty()) return;

        // 이미지 메타데이터를 File 엔티티로 변환하여 저장
        List<File> files = IntStream.range(0, images.size())
                .mapToObj(i -> File.create(images.get(i), refType, refId, i))
                .toList();

        fileRepository.saveAll(files); // 일괄 등록
    }

    /**
     * 지정한 게시물(refType, refId)의 기존 첨부를 모두 삭제한 뒤, 새 목록으로 등록합니다.
     * images가 null이면 호출하지 말 것. 빈 목록이면 삭제만 하고 등록은 하지 않습니다.
     *
     * @param refType 첨부 대상 유형 (STORE, REVIEW)
     * @param refId   첨부 대상 ID (상점 ID 또는 리뷰 ID)
     * @param images  새로 등록할 파일 정보 (fileKey, filePath, displayOrder), 빈 목록 가능
     */
    @Transactional
    public void replaceFiles(AttachmentType refType, Long refId, List<ImageMetadata> images) {
        fileRepository.deleteByRefTypeAndRefId(refType, refId);
        if (images != null && !images.isEmpty()) {
            registerFiles(refType, refId, images);
        }
    }
}
