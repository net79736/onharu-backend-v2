package com.backend.onharu.interfaces.api.controller.impl;

import com.backend.onharu.application.LevelFacade;
import com.backend.onharu.domain.level.dto.LevelCommand.CreateLevelCommand;
import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.controller.ILevelController;
import com.backend.onharu.interfaces.api.dto.LevelControllerDto.CreateLevelRequest;
import com.backend.onharu.interfaces.api.dto.LevelControllerDto.CreateLevelResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.backend.onharu.domain.level.dto.LevelCommand.UpdateNameByIdCommand;
import static com.backend.onharu.domain.level.dto.LevelQuery.GetLevelByIdQuery;
import static com.backend.onharu.interfaces.api.dto.LevelControllerDto.LevelResponse;
import static com.backend.onharu.interfaces.api.dto.LevelControllerDto.UpdateLevelRequest;

/**
 * 등급 관련 API 를 제거하는 컨트롤러 구현체 입니다.
 * <p>
 * 등급 생성 기능을 제공합니다.
 */
@Slf4j
@RestController
@RequestMapping("/api/levels")
@RequiredArgsConstructor
public class LevelControllerImpl implements ILevelController {

    private final LevelFacade levelFacade;

    /**
     * 등급 생성
     * POST /api/levels
     * 신규 등급 생성을 진행합니다. 등급명 정보를 받습니다.
     *
     * @param request 등급 생성 요청
     * @return 등급 ID
     */
    @Override
    @PostMapping
    public ResponseEntity<ResponseDTO<CreateLevelResponse>> createLevel(
            @Valid @RequestBody CreateLevelRequest request
    ) {
        log.info("등급 생성 요청: request={}", request);

        // Command 생성
        CreateLevelCommand command = new CreateLevelCommand(
                request.name(),
                request.conditionNumber()
        );

        Level level = levelFacade.createLevel(command);

        CreateLevelResponse response = new CreateLevelResponse(
                level.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
    }

    /**
     * 등급 단일 조회
     * GET /api/levels/{levelId}
     * 특정 등급을 조회합니다. 등급 ID 를 받습니다.
     *
     * @param levelId 등급 ID
     * @return 등급 정보가
     */
    @Override
    @GetMapping("/{levelId}")
    public ResponseEntity<ResponseDTO<LevelResponse>> getLevel(
            @PathVariable Long levelId) {
        log.info("등급 단일 조회 요청: levelId={}", levelId);

        // Query 생성
        GetLevelByIdQuery query = new GetLevelByIdQuery(levelId);
        // 등급 단일 조회
        Level level = levelFacade.getLevel(query);

        // 응답 생성
        LevelResponse response = new LevelResponse(
                level.getId(),
                level.getName()
        );

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 등급 목록 조회
     * GET /api/levels
     * 전체 등급 목록을 조회합니다.
     *
     * @return 등급 목록
     */
    @Override
    @GetMapping
    public ResponseEntity<ResponseDTO<List<LevelResponse>>> getLevels() {
        log.info("등급 목록 조회");

        // 등급 목록 조회
        List<Level> levels = levelFacade.getLevels();

        // 응답 리스트 생성
        List<LevelResponse> response = levels.stream()
                .map(level ->
                        new LevelResponse(
                                level.getId(),
                                level.getName()
                        )
                ).toList();

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }

    /**
     * 등급 정보 수정
     * PUT /api/levels
     * <p>
     * 등급 ID 를 기준으로 등급명과 등급 조건 횟수를 수정합니다.
     */
    @Override
    @PutMapping
    public ResponseEntity<ResponseDTO<String>> updateLevel(UpdateLevelRequest request) {
        log.info("등급 정보 수정");

        // 등급 정보 수정
        levelFacade.updateLevel(
                new UpdateNameByIdCommand(
                        request.levelId(),
                        request.levelName(),
                        request.conditionNumber()
                )
        );

        // 응답 생성
        String response = "등급 수정 성공";

        return ResponseEntity.status(HttpStatus.OK)
                .body(ResponseDTO.success(response));
    }
}
