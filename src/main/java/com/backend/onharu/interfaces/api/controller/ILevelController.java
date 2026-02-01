package com.backend.onharu.interfaces.api.controller;

import com.backend.onharu.interfaces.api.common.dto.ResponseDTO;
import com.backend.onharu.interfaces.api.dto.LevelControllerDto.CreateLevelRequest;
import com.backend.onharu.interfaces.api.dto.LevelControllerDto.CreateLevelResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Level", description = "등급 API")
public interface ILevelController {

    /**
     * 등급 생성
     *
     * POST /levels
     * 등급 생성을 진행합니다. 등급명 정보를 받습니다.
     *
     * @param request 등급명
     * @return 등급 ID(식별번호)
     */
    @Operation(summary = "등급 생성", description = "등급 정보를 등록합니다. 등급명을 받습니다.")
    ResponseEntity<ResponseDTO<CreateLevelResponse>> createLevel(
            @RequestBody(
                    description = "등급 생성 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = CreateLevelRequest.class),
                            examples = @ExampleObject(
                                    name = "등급 생성 예시",
                                    value = "{\n" +
                                            "  \"name\": \"새싹\",\n" +
                                            "}"
                            )
                    )
            )
            CreateLevelRequest request
    );
}
