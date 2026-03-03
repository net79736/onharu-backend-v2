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

import java.util.List;

import static com.backend.onharu.interfaces.api.dto.LevelControllerDto.LevelResponse;
import static com.backend.onharu.interfaces.api.dto.LevelControllerDto.UpdateLevelRequest;

@Tag(name = "Level", description = "등급 API")
public interface ILevelController {

    /**
     * 등급 생성
     * <p>
     * POST /api/levels
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
                            examples = {
                                    @ExampleObject(
                                            name = "등급 생성 예시1(예약완료 0회)",
                                            value = """
                                                    {
                                                      "name": "비기너",
                                                      "conditionNumber": "0"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "등급 생성 예시2(예약완료 1~4회)",
                                            value = """
                                                    {
                                                      "name": "새싹",
                                                      "conditionNumber": "1"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "등급 생성 예시2(예약완료 5~9회)",
                                            value = """
                                                    {
                                                      "name": "새싹2",
                                                      "conditionNumber": "5"
                                                    }
                                                    """
                                    ),
                                    @ExampleObject(
                                            name = "등급 생성 예시2(예약완료 10~19회)",
                                            value = """
                                                    {
                                                      "name": "새싹3",
                                                      "conditionNumber": "10"
                                                    }
                                                    """
                                    ),
                            }
                    )
            )
            CreateLevelRequest request
    );

    /**
     * 등급 단일 조회
     * <p>
     * GET /api/levels/{levelId}
     * 특정 등급을 조회합니다. 등급 ID 를 받습니다.
     *
     * @param levelId 등급 ID
     * @return 등급 ID, 등급명
     */
    @Operation(summary = "등급 조회", description = "등급 ID 로 등급을 조회합니다.")
    ResponseEntity<ResponseDTO<LevelResponse>> getLevel(
            @Schema(name = "등급 ID", description = "조회할 등급 ID")
            Long levelId
    );

    /**
     * 등급 목록 조회
     * <p>
     * GET /api/levels
     * 전체 등급 목록을 조회합니다.
     *
     * @return 등급 목록
     */
    @Operation(summary = "등급 목록 조회", description = "전체 등급 목록을 조회합니다.")
    ResponseEntity<ResponseDTO<List<LevelResponse>>> getLevels(
    );

    /**
     * 등급 수정
     * <p>
     * PUT /api/levels
     */
    @Operation(summary = "등급 수정", description = "등급 정보를 수정합니다.")
    ResponseEntity<ResponseDTO<String>> updateLevel(
            @RequestBody(
                    description = "등급 수정 요청",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = UpdateLevelRequest.class),
                            examples = {
                                    @ExampleObject(
                                            name = "등급 수정 예시",
                                            value = """
                                                    {
                                                      "levelId": "1"
                                                      "levelName": "새싹",
                                                      "conditionNumber": "10"
                                                    }
                                                    """
                                    )
                            }
                    )
            )
            UpdateLevelRequest request
    );
}
