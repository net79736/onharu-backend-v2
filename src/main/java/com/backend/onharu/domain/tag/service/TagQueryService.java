package com.backend.onharu.domain.tag.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.backend.onharu.domain.tag.dto.TagQuery;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.GetTagByIdParam;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.domain.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TagQueryService {
    private final TagRepository tagRepository;

    /**
     * 태그 단건 조회
     * @param id 태그 식별자
     * @return 조회된 Tag 엔티티 (없으면 예외 발생)
     */
    public Tag findById(TagQuery.GetTagByIdQuery query) {
        return tagRepository.getTag(new GetTagByIdParam(query.tagId()));
    }

    /**
     * 태그 전체 조회 (여러 건)
     * @return 전체 태그 리스트
     */
    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    /**
     * 이름으로 태그 목록 조회
     * @param name 태그 이름
     * @return 이름이 일치하는 태그 리스트
     */
    public List<Tag> findAllByName(String name) {
        return tagRepository.findAllByName(name);
    }
}