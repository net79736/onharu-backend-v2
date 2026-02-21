package com.backend.onharu.application;

import com.backend.onharu.domain.level.dto.LevelCommand.CreateLevelCommand;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.service.LevelCommandService;
import com.backend.onharu.domain.level.service.LevelQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

import static com.backend.onharu.domain.level.dto.LevelQuery.*;

/**
 * 등급 Facade
 */
@Component
@RequiredArgsConstructor
public class LevelFacade {

    private final LevelCommandService levelCommandService;
    private final LevelQueryService levelQueryService;

    /**
     * 등급 생성을 처리합니다.
     *
     * @param command 등급 생성 Command
     * @return 생성된 등급 엔티티
     */
    public Level createLevel(CreateLevelCommand command) {
        return levelCommandService.createLevel(command);
    }

    /**
     * 등급 단일 조회
     *
     * @param query 등급 ID 가 포함된 query
     * @return 조회된 등급 엔티티
     */
    public Level getLevel(GetLevelByIdQuery query) {
        return levelQueryService.getLevel(query);
    }

    /**
     * 등급 목록 조회
     *
     * @return 조회된 등급 목록
     */
    public List<Level> getLevels() {
        return levelQueryService.getLevels();
    }

}
