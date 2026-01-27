package com.backend.onharu.domain.child.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.child.dto.ChildCommand.CreateChildCommand;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.repository.ChildRepository;

import lombok.RequiredArgsConstructor;

/**
 * 아동 Command Service
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ChildCommandService {

    private final ChildRepository childRepository;

    /**
     * 아동을 생성합니다.
     * 
     * @param command 아동 생성 Command
     * @return 생성된 아동 엔티티
     */
    public Child createChild(CreateChildCommand command) {
        Child child = Child.builder()
                .user(command.user())
                .certificate(command.certificate())
                .isVerified(false) // 초기값은 미승인 상태
                .build();

        return childRepository.save(child);
    }
}
