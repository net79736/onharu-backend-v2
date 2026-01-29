package com.backend.onharu.domain.tag.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.tag.dto.TagCommand.CreateTagCommand;
import com.backend.onharu.domain.tag.dto.TagCommand.DeleteTagCommand;
import com.backend.onharu.domain.tag.dto.TagCommand.UpdateTagCommand;
import com.backend.onharu.domain.tag.dto.TagRepositroyParam.GetTagByIdParam;
import com.backend.onharu.domain.tag.model.Tag;
import com.backend.onharu.domain.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TagCommandService {
    private final TagRepository tagRepository;

    /**
     * 태그 생성
     */
    public Tag createTag(CreateTagCommand command) {
        Tag tag = Tag.builder()
                .name(command.name())
                .build();

        return tagRepository.save(tag);
    }

    /**
     * 태그 정보 수정
     */
    public void updateTag(UpdateTagCommand command) {
        Tag tag = tagRepository.getTag(new GetTagByIdParam(command.id()));
        
        tag.updateName(command.name()); // 도메인 모델 내 업데이트 로직 호출
    }

    /**
     * 태그 삭제
     */
    public void deleteTag(DeleteTagCommand command) {
        Tag tag = tagRepository.getTag(new GetTagByIdParam(command.id()));
        
        tagRepository.delete(tag);
    }
}
