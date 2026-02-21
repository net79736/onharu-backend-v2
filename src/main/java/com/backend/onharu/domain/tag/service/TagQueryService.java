package com.backend.onharu.domain.tag.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.tag.dto.TagQuery.FindByIdsQuery;
import com.backend.onharu.domain.tag.dto.TagQuery.FindByNameQuery;
import com.backend.onharu.domain.tag.dto.TagQuery.GetTagByIdQuery;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.FindByIdsParam;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.FindByNameParam;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.GetTagByIdParam;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.domain.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TagQueryService {
    private final TagRepository tagRepository;

    /**
     * 태그 단건 조회
     * @param id 태그 식별자
     * @return 조회된 Tag 엔티티 (없으면 예외 발생)
     */
    public Tag getTag(GetTagByIdQuery query) {
        return tagRepository.getTag(new GetTagByIdParam(query.tagId()));
    }

    /**
     * 이름으로 태그 목록 조회
     * @param name 태그 이름
     * @return 이름이 일치하는 태그 리스트
     */
    public List<Tag> findByName(FindByNameQuery query) {
        return tagRepository.findAllByName(new FindByNameParam(query.name()));
    }

    /**
     * ID 리스트로 여러 태그 조회
     * @param query ID 리스트
     * @return ID 리스트에 해당하는 태그 리스트
     */
    public List<Tag> findAllByIds(FindByIdsQuery query) {
        return tagRepository.findAllByIds(new FindByIdsParam(query.tagIds()));
    }
}