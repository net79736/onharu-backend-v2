package com.backend.onharu.infra.db.file.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.common.enums.AttachmentType;
import com.backend.onharu.domain.file.model.File;
import com.backend.onharu.domain.file.repository.FileRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.infra.db.file.FileJpaRepository;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class FileRepositoryImpl implements FileRepository {

    private final FileJpaRepository fileJpaRepository;

    @Override
    public File save(File file) {
        return fileJpaRepository.save(file);
    }

    @Override
    public List<File> saveAll(List<File> files) {
        return fileJpaRepository.saveAll(files);
    }

    @Override
    public void delete(File file) {
        fileJpaRepository.delete(file);
    }

    @Override
    public File getById(Long id) {
        return fileJpaRepository.findById(id)
                .orElseThrow(() -> new CoreException(ErrorType.FileOperation.FILE_NOT_FOUND));
    }

    @Override
    public File getByFileKey(String fileKey) {
        return fileJpaRepository.findByFileKey(fileKey)
                .orElseThrow(() -> new CoreException(ErrorType.FileOperation.FILE_NOT_FOUND));
    }

    @Override
    public List<File> findByRefTypeAndRefIdOrderByDisplayOrderAsc(AttachmentType refType, Long refId) {
        return fileJpaRepository.findByRefTypeAndRefIdOrderByDisplayOrderAsc(refType, refId);
    }

    @Override
    public void deleteByRefTypeAndRefId(AttachmentType refType, Long refId) {
        fileJpaRepository.deleteByRefTypeAndRefId(refType, refId);
    }
}
