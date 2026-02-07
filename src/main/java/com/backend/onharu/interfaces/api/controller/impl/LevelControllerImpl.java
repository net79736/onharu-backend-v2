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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 등급 관련 API 를 제거하는 컨트롤러 구현체 입니다.
 *
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
     *
     * POST /levels
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
                request.name()
        );

        Level level = levelFacade.createLevel(command);

        CreateLevelResponse response = new CreateLevelResponse(
                level.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ResponseDTO.success(response));
    }

}
