package com.backend.onharu.interfaces.api.controller.impl;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.backend.onharu.domain.upload.service.StorageService;
import com.backend.onharu.interfaces.api.dto.S3ControllerDto.S3FileUploadResponse;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("S3Controller MockMvc 테스트")
class S3ControllerImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StorageService storageService;

    @Nested
    @DisplayName("GET /api/upload")
    class GeneratePresignedUrl {

        @Test
        @DisplayName("presigned URL 생성 요청 시 Storage 가 반환한 URL 을 응답")
        void generate_returnsUrl() throws Exception {
            given(storageService.generatePresignedUrl(anyString(), anyString()))
                    .willReturn(new S3FileUploadResponse(
                            "https://example.com/upload",
                            "https://example.com/download/obj-key"));

            mockMvc.perform(get("/api/upload")
                            .param("fileName", "photo.jpg")
                            .param("contentType", "image/jpeg"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.presignedUrl").value("https://example.com/upload"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/upload/delete")
    class DeleteFile {

        @Test
        @DisplayName("파일 삭제 요청 200")
        void delete_returnsOk() throws Exception {
            given(storageService.deleteFile(anyString())).willReturn(true);

            mockMvc.perform(delete("/api/upload/delete").param("fileName", "image/abc.jpg"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/upload/download")
    class DownloadFile {

        @Test
        @DisplayName("유효한 fileName — 파일 바이트와 Content-Disposition 헤더 반환")
        void download_success() throws Exception {
            byte[] body = "hello-world".getBytes();
            given(storageService.downloadFile("image/uuid-photo.jpg")).willReturn(body);
            given(storageService.getContentType("image/uuid-photo.jpg")).willReturn("image/jpeg");

            mockMvc.perform(get("/api/upload/download").param("fileName", "image/uuid-photo.jpg"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/jpeg"))
                    .andExpect(header().exists("Content-Disposition"));
        }

        @Test
        @DisplayName("비어있는 fileName 은 400")
        void download_blankFileName_returnsBadRequest() throws Exception {
            mockMvc.perform(get("/api/upload/download").param("fileName", ""))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("ContentType 이 application/octet-stream 이면 파일 확장자로 추론 (gif)")
        void download_inferContentTypeFromExtension() throws Exception {
            byte[] body = "gif-bytes".getBytes();
            given(storageService.downloadFile("image/file.gif")).willReturn(body);
            given(storageService.getContentType("image/file.gif")).willReturn("application/octet-stream");

            mockMvc.perform(get("/api/upload/download").param("fileName", "image/file.gif"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "image/gif"));
        }

        @Test
        @DisplayName("PDF 확장자 추론")
        void download_inferPdf() throws Exception {
            byte[] body = "pdf-bytes".getBytes();
            given(storageService.downloadFile("doc/report.pdf")).willReturn(body);
            given(storageService.getContentType("doc/report.pdf")).willReturn(null);

            mockMvc.perform(get("/api/upload/download").param("fileName", "doc/report.pdf"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Content-Type", "application/pdf"));
        }

        @Test
        @DisplayName("Storage 가 '찾을 수 없습니다' 메시지 예외 — 404")
        void download_notFound_returns404() throws Exception {
            given(storageService.downloadFile(anyString()))
                    .willThrow(new RuntimeException("파일을 찾을 수 없습니다"));

            mockMvc.perform(get("/api/upload/download").param("fileName", "image/missing.jpg"))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Storage 기타 RuntimeException — 500")
        void download_storageError_returns500() throws Exception {
            given(storageService.downloadFile(anyString()))
                    .willThrow(new RuntimeException("S3 offline"));

            mockMvc.perform(get("/api/upload/download").param("fileName", "image/err.jpg"))
                    .andExpect(status().isInternalServerError());
        }
    }
}
