package com.backend.onharu.infra.db.level.impl;

import com.backend.onharu.domain.level.dto.LevelRepositoryParam;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.repository.LevelRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

/**
 * 등급 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class LevelRepositoryImpl implements LevelRepository {

    private final LevelJpaRepository levelJpaRepository;

    @Override
    public Level save(Level level) {
        return levelJpaRepository.save(level);
    }

    @Override
    public Level getLevel(LevelRepositoryParam.GetLevelByIdParam param) {
        return levelJpaRepository.findById(param.id())
                .orElseThrow(() -> new CoreException(ErrorType.Level.LEVEL_NOT_FOUND));
    }

    @Override
    public Level getLevelByName(LevelRepositoryParam.GetLevelByNameParam param) {
        return levelJpaRepository.findByName(param.name())
                .orElseThrow(() -> new CoreException(ErrorType.Level.NAME_MUST_NOT_BE_BLANK));
    }
}
