package com.backend.onharu.domain.level.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.level.dto.LevelCacheDto;
import com.backend.onharu.domain.level.dto.LevelQuery.FindFirstByConditionNumberQuery;
import com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByIdQuery;
import com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByNameQuery;
import com.backend.onharu.domain.level.dto.LevelRepositoryParam.FindFirstByConditionNumberParam;
import com.backend.onharu.domain.level.dto.LevelRepositoryParam.GetLevelByIdParam;
import com.backend.onharu.domain.level.dto.LevelRepositoryParam.GetLevelByNameParam;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.repository.LevelRepository;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.infra.redis.cache.LevelHashCacheRepository;

import lombok.RequiredArgsConstructor;

/**
 * 등급 Query Service
 * <p>
 * 등급 도메인의 상태를 조회하는 비즈니스 로직을 처리하는 서비스입니다.
 * Query 패턴을 사용하여 도메인 조회 작업을 캡슐화합니다.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LevelQueryService {

    private final LevelRepository levelRepository;
    private final LevelHashCacheRepository levelHashCacheRepository;

    /**
     * 등급 ID로 등급을 조회합니다.
     *
     * @param query 등급 ID를 포함한 Query
     * @return 조회된 등급 엔티티
     * @throws CoreException 등급을 찾을 수 없는 경우
     */
    public Level getLevel(GetLevelByIdQuery query) {
        return levelRepository.getLevel(new GetLevelByIdParam(query.id()));
    }

    /**
     * 등급명으로 등급을 조회합니다.
     *
     * @param query 등급명을 포함한 Query
     * @return 조회된 등급 엔티티
     * @throws CoreException 등급을 찾을 수 없는 경우
     */
    public Level getLevelByName(GetLevelByNameQuery query) {
        return levelRepository.getLevelByName(new GetLevelByNameParam(query.name()));
    }

    /**
     * 전체 등급 목록을 조회합니다. (캐시용: DTO로 평탄화)
     *
     * @return 조회된 등급 목록
     */
    public List<LevelCacheDto> getLevelsCache() {
        List<LevelCacheDto> cached = levelHashCacheRepository.getAll();
        if (!cached.isEmpty()) {
            return cached;
        }
        List<Level> levels = levelRepository.getLevels();
        List<LevelCacheDto> mapped = levels.stream().map(LevelCacheDto::from).toList();
        levelHashCacheRepository.putAll(mapped);
        return mapped;
    }

    /**
     * 현재 등급의 등급 조건 횟수로 다음 등급을 조회합니다.
     *
     * @param query 등급 조건 횟수를 포함한 Query
     * @return 다음 등급 객체(Optional 로 감싸져 있음)
     */
    public Optional<Level> findFirstByConditionNumber(FindFirstByConditionNumberQuery query) {
        return levelRepository.findFirstByConditionNumber(new FindFirstByConditionNumberParam(query.distributionCount()));
    }
}
