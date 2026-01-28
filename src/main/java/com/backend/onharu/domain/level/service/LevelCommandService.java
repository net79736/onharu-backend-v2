package com.backend.onharu.domain.level.service;

import com.backend.onharu.domain.level.dto.LevelCommand.CreateLevelCommand;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.repository.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 등급 Command Service
 *
 * 등급 도메인의 상태를 변경하는 비즈니스 로직을 처리하는 서비스 입니다.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class LevelCommandService {

    private final LevelRepository levelRepository;

    /**
     * 등급을 생성합니다.
     *
     * @param command 등급 생성 Command
     * @return 생성된 등급 엔티티
     */
    public Level createLevel(CreateLevelCommand command) {
        Level level = Level.builder()
                .name(command.name())
                .build();

        return levelRepository.save(level);
    }
}
