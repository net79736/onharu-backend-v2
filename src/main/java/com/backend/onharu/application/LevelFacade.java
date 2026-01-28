package com.backend.onharu.application;

import com.backend.onharu.domain.level.dto.LevelCommand.CreateLevelCommand;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.service.LevelCommandService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 등급 Facade
 */
@Component
@RequiredArgsConstructor
public class LevelFacade {

    private final LevelCommandService levelCommandService;

    /**
     * 등급 생성을 처리합니다.
     *
     * @param command 등급 생성 Command
     * @return 생성된 등급 엔티티
     */
    public Level createLevel(CreateLevelCommand command) {
        return levelCommandService.createLevel(command);
    }
}
