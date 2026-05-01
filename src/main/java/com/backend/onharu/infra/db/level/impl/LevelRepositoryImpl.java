package com.backend.onharu.infra.db.level.impl;

import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.repository.LevelRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import static com.backend.onharu.domain.level.dto.LevelRepositoryParam.*;
import static com.backend.onharu.domain.support.error.ErrorType.Level.*;

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
    public List<Level> getLevels() {
        return levelJpaRepository.findAll();
    }

    @Override
    public Level getLevel(GetLevelByIdParam param) {
        return levelJpaRepository.findById(param.id())
                .orElseThrow(() -> new CoreException(LEVEL_NOT_FOUND));
    }

    @Override
    public Level getLevelByName(GetLevelByNameParam param) {
        return levelJpaRepository.findByName(param.name())
                .orElseThrow(() -> new CoreException(LEVEL_NAME_NOT_FOUND));
    }

    @Override
    public void updateNameById(UpdateNameByIdParam param) {
        levelJpaRepository.updateNameById(param.id(), param.name(), param.conditionNumber());
    }

    @Override
    public Optional<Level> findFirstByConditionNumber(FindFirstByConditionNumberParam param) {
        return levelJpaRepository.findFirstByConditionNumberGreaterThanOrderByConditionNumberAsc(param.conditionNumber());
    }

    @Override
    public void deleteLevel(Level level) {
        levelJpaRepository.delete(level);
    }
}
