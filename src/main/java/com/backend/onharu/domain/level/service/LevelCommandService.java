package com.backend.onharu.domain.level.service;

import com.backend.onharu.domain.level.dto.LevelCommand.CreateLevelCommand;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.domain.level.repository.LevelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.onharu.domain.level.dto.LevelCommand.UpdateNameByIdCommand;
import static com.backend.onharu.domain.level.dto.LevelRepositoryParam.UpdateNameByIdParam;

/**
 * 등급 Command Service
 * <p>
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
                .conditionNumber(command.conditionNumber())
                .build();

        return levelRepository.save(level);
    }

    /**
     * 등급 정보를 수정합니다.
     *
     * @param command 등급명, 등급 ID 를 포함한 등급 수정 Command
     */
    public void updateNameById(UpdateNameByIdCommand command) {
        levelRepository.updateNameById(
                new UpdateNameByIdParam(
                        command.id(),
                        command.name(),
                        command.conditionNumber()
                )
        );
    }

    /**
     * 변경된 등급의 정보를 DB 에 반영합니다.
     * @param level 등급 엔티티
     */
    public void updateLevel(Level level) {
        levelRepository.save(level);
    }

    /**
     * 등급 엔티티를 제거합니다.
     */
    public void deleteLevel(Level level) {
        levelRepository.deleteLevel(level);
    }
}
