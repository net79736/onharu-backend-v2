package com.backend.onharu.interfaces.api.controller.impl;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.level.model.Level;
import com.backend.onharu.infra.db.level.LevelJpaRepository;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("LevelController MockMvc 테스트")
class LevelControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private LevelJpaRepository levelJpaRepository;

    @Test
    @DisplayName("POST /api/levels — 등급을 생성하고 201 + levelId 를 반환한다")
    void createLevel_returnsCreated() throws Exception {
        String body = """
                {
                  "name": "새싹-테스트",
                  "conditionNumber": 1
                }
                """;

        mockMvc.perform(post("/api/levels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.levelId").exists());
    }

    @Test
    @DisplayName("POST /api/levels — name 이 blank 면 400")
    void createLevel_blankName_returnsBadRequest() throws Exception {
        String body = """
                {
                  "name": "",
                  "conditionNumber": 1
                }
                """;

        mockMvc.perform(post("/api/levels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/levels/{id} — 존재하는 등급 조회 시 name 포함 응답")
    void getLevel_returnsLevel() throws Exception {
        Level saved = levelJpaRepository.save(Level.builder().name("브론즈").build());

        mockMvc.perform(get("/api/levels/{id}", saved.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.levelId").value(saved.getId()))
                .andExpect(jsonPath("$.data.name").value("브론즈"));
    }

    @Test
    @DisplayName("GET /api/levels — 등급 목록 조회")
    void getLevels_returnsList() throws Exception {
        levelJpaRepository.save(Level.builder().name("L1").build());
        levelJpaRepository.save(Level.builder().name("L2").build());

        mockMvc.perform(get("/api/levels"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("PUT /api/levels — 등급 이름 수정")
    void updateLevel_returnsOk() throws Exception {
        Level saved = levelJpaRepository.save(Level.builder().name("원본").build());
        String body = """
                {
                  "levelId": %d,
                  "levelName": "수정됨",
                  "conditionNumber": 5
                }
                """.formatted(saved.getId());

        mockMvc.perform(put("/api/levels")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").value("등급 수정 성공"));
    }

    @Test
    @DisplayName("DELETE /api/levels/{id} — 삭제 요청은 204 응답")
    void deleteLevel_returnsNoContent() throws Exception {
        Level saved = levelJpaRepository.save(Level.builder().name("삭제대상").build());

        mockMvc.perform(delete("/api/levels/{id}", saved.getId()))
                .andExpect(status().isNoContent());
    }
}
