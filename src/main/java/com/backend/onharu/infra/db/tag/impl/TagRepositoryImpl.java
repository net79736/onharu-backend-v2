package com.backend.onharu.infra.db.tag.impl;

import java.util.List;

import org.springframework.stereotype.Repository;

import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.FindByIdsParam;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.FindByNameParam;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.GetTagByIdParam;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.domain.tag.repository.TagRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;

import lombok.RequiredArgsConstructor;

/**
 * 태그 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class TagRepositoryImpl implements TagRepository {

    private final TagJpaRepository tagJpaRepository;

    @Override
    public Tag save(Tag tag) {
        return tagJpaRepository.save(tag);
    }

    @Override
    public Tag getTag(GetTagByIdParam param) {
        return tagJpaRepository.findById(param.tagId())
                .orElseThrow(() -> new CoreException(ErrorType.Tag.TAG_NOT_FOUND));
    }

    @Override
    public List<Tag> findAllByIds(FindByIdsParam param) {
        return tagJpaRepository.findAllById(param.tagIds());
    }

    @Override
    public List<Tag> findAllByName(FindByNameParam param) {
        return tagJpaRepository.findAllByName(param.name());
    }

    @Override
    public void delete(Tag tag) {
        tagJpaRepository.delete(tag);
    }
}
