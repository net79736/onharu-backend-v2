package com.backend.onharu.domain.file.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.file.dto.FileCommand.DeleteFileCommand;
import com.backend.onharu.domain.file.dto.FileCommand.RegisterFileCommand;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.repository.FileRepository;

import lombok.RequiredArgsConstructor;

/**
 * 파일 메타데이터 등록/삭제 서비스
 *
 * S3 업로드는 기존 S3 Presigned URL API를 사용하고,
 * 업로드 완료 후 이 서비스로 DB에 파일 정보를 등록합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class FileCommandService {

    private final FileRepository fileRepository;

    /**
     * 파일 메타데이터 등록 (Presigned URL 업로드 완료 후 호출)
     */
    public File register(RegisterFileCommand command) {
        File file = File.builder()
                .fileKey(command.fileKey())
                .storedFileName(command.storedFileName())
                .filePath(command.filePath())
                .fileExtension(command.fileExtension())
                .fileSize(command.fileSize())
                .refType(command.refType())
                .refId(command.refId())
                .displayOrder(command.displayOrder() != null ? command.displayOrder() : 0)
                .build();
        return fileRepository.save(file);
    }

    /**
     * 파일 메타데이터 삭제 (DB만 삭제. S3 객체 삭제는 호출부에서 StorageService.deleteFile(fileKey) 호출)
     * 없으면 Repository에서 FILE_NOT_FOUND 예외.
     */
    public void delete(DeleteFileCommand command) {
        File file = fileRepository.getById(command.id());
        fileRepository.delete(file);
    }
}
