package com.backend.onharu.application;

import static com.backend.onharu.domain.support.error.ErrorType.FileOperation.CSV_PROCESSING_ERROR;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.backend.onharu.domain.store.dto.StoreCommand.CreateStoreCommand;
import com.backend.onharu.domain.support.error.CoreException;
import com.backend.onharu.domain.support.error.ErrorType;

import jakarta.validation.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 상점 엑셀 업로드 전용 Facade.
 *
 * - 엑셀 파싱(POI)을 담당하고
 * - 파싱된 한 행(row)을 CreateStoreCommand 로 매핑한 뒤
 * - 실제 저장은 StoreFacade.createStore(...) 에 위임합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class StoreExcelFacade {

    private final StoreFacade storeFacade;

    /**
     * 엑셀 파일을 읽어 가게 정보를 일괄 등록합니다.
     *
     * 엑셀 컬럼 포맷(0-based index):
     * 0: 카테고리 ID (Long)
     * 1: 가게 이름 (String)
     * 2: 주소 (String)
     * 3: 전화번호 (String)
     * 4: 위도 (String)
     * 5: 경도 (String)
     * 6: 가게 소개 (String)
     * 7: 한줄 소개 (String)
     * 8: 태그 목록 (쉼표로 구분된 문자열, 예: "커피, 디저트, 브런치")
     *
     * 첫 번째 행은 헤더로 간주하고 두 번째 행부터 데이터를 읽습니다.
     *
     * @param file    업로드된 엑셀 파일
     * @param ownerId 현재 로그인한 사업자 ID (모든 행에 동일하게 적용)
     * @return int[3] = {전체 행 수, 성공 수, 실패 수}
     */
    // @Transactional(propagation = Propagation.REQUIRES_NEW)
    public int[] importStoresFromExcel(MultipartFile file, Long ownerId) {
        log.info("importStoresFromExcel: file={}, fileName={}, ownerId={}", file, file.getOriginalFilename(), ownerId);
        if (file == null || file.isEmpty()) {
            return new int[] {0, 0, 0};
        }

        if (ownerId == null) {
            throw new CoreException(ErrorType.Owner.OWNER_ID_MUST_NOT_BE_NULL);
        }

        // 파일 확장자 검증
        verifyMimeType(file.getOriginalFilename());

        // 파일 확장자에 따라 엑셀(xls/xlsx) 또는 CSV 모두 지원
        String originalFilename = file.getOriginalFilename();
        if (originalFilename != null) {
            log.info("originalFilename: {}", originalFilename);    
            String lowerName = originalFilename.toLowerCase();
            if (lowerName.endsWith(".csv")) {
                log.info("CSV 파일 업로드 처리");
                return importStoresFromCsv(file, ownerId);
            } else {
                log.info("엑셀 파일 업로드 처리");
                return importStoresFromWorkbook(file, ownerId);
            }
        }

        return new int[] {0, 0, 0};
    }

    /**
     * Apache POI 를 사용해 xls/xlsx 엑셀 파일을 처리합니다.
     */
    private int[] importStoresFromWorkbook(MultipartFile file, Long ownerId) {
        int totalCount = 0;
        int successCount = 0;
        int failureCount = 0;

        try (InputStream inputStream = file.getInputStream();
             Workbook workbook = WorkbookFactory.create(inputStream)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return new int[] {0, 0, 0};
            }

            int lastRowNum = sheet.getLastRowNum();
            // 0번째 행은 헤더로 간주, 1부터 시작
            for (int rowIndex = 1; rowIndex <= lastRowNum; rowIndex++) {
                Row row = sheet.getRow(rowIndex);
                if (row == null) {
                    continue;
                }

                // 필수 컬럼(가게명)이 비어있으면 스킵
                String name = getStringCellValue(row.getCell(1));
                if (name == null || name.isBlank()) {
                    continue;
                }

                totalCount++;
                try {
                    Long categoryId = getLongCellValue(row.getCell(0));
                    String address = getStringCellValue(row.getCell(2));
                    String phone = getStringCellValue(row.getCell(3));
                    String lat = getStringCellValue(row.getCell(4));
                    String lng = getStringCellValue(row.getCell(5));
                    String introduction = getStringCellValue(row.getCell(6));
                    String intro = getStringCellValue(row.getCell(7));
                    String tagsRaw = getStringCellValue(row.getCell(8));

                    List<String> tagNames = parseTagNames(tagsRaw);

                    CreateStoreCommand command = new CreateStoreCommand(
                            ownerId,
                            categoryId,
                            name,
                            address,
                            phone,
                            lat,
                            lng,
                            introduction,
                            intro,
                            tagNames,
                            List.of(), // 엑셀로는 영업시간은 받지 않음
                            List.of()  // 이미지도 엑셀로는 받지 않음
                    );

                    storeFacade.createStore(command, ownerId);
                    successCount++;
                } catch (Exception e) {
                    // 한 행 처리 실패 시 해당 행만 실패로 카운팅하고 계속 진행
                    failureCount++;
                }
            }
        } catch (IOException e) {
            // 전체 파일 읽기 실패 시 런타임 예외로 래핑
            throw new RuntimeException("엑셀 파일을 처리하는 중 오류가 발생했습니다.", e);
        }

        return new int[] {totalCount, successCount, failureCount};
    }

    /**
     * CSV 파일을 읽어 가게 정보를 일괄 등록합니다.
     * 첫 줄은 헤더, 두 번째 줄부터 데이터를 처리합니다.
     */
    private int[] importStoresFromCsv(MultipartFile file, Long ownerId) {
        log.info("CSV 파일 업로드 처리");
        int totalCount = 0;
        int successCount = 0;
        int failureCount = 0;

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {

            String line;
            boolean isHeader = true;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue;
                }
                // 첫 줄은 헤더로 간주
                if (isHeader) {
                    isHeader = false;
                    continue;
                }

                List<String> columns = parseCsvLine(line);
                // 최소 9개 컬럼(categoryId~tagNames)이 아니면 스킵
                if (columns.size() < 9) {
                    continue;
                }

                String name = safeGet(columns, 1);
                if (name == null || name.isBlank()) {
                    continue;
                }

                totalCount++;
                try {
                    Long categoryId = parseLongSafe(safeGet(columns, 0));
                    String address = safeGet(columns, 2);
                    String phone = safeGet(columns, 3);
                    String lat = safeGet(columns, 4);
                    String lng = safeGet(columns, 5);
                    String introduction = safeGet(columns, 6);
                    String intro = safeGet(columns, 7);
                    String tagsRaw = safeGet(columns, 8);

                    List<String> tagNames = parseTagNames(tagsRaw);

                    CreateStoreCommand command = new CreateStoreCommand(
                            ownerId,
                            categoryId,
                            name,
                            address,
                            phone,
                            lat,
                            lng,
                            introduction,
                            intro,
                            tagNames,
                            List.of(), // CSV로는 영업시간은 받지 않음
                            List.of()  // 이미지도 CSV로는 받지 않음
                    );

                    log.info("가게 생성: {}", command);
                    storeFacade.createStore(command, ownerId);
                    log.info("가게 생성 성공");
                    successCount++;
                } catch (Exception e) {
                    log.error("가게 생성 실패 클래스: {}", e.getClass());
                    log.error("가게 생성 실패 원인: {}", e.getCause());
                    log.error("가게 생성 실패 메시지: {}", e.getMessage());
                    log.error("가게 생성 실패 예외: {}", e);
                    failureCount++;
                }
            }
        } catch (IOException e) {
            log.error("CSV 파일을 처리하는 중 오류가 발생했습니다.", e);
            throw new CoreException(CSV_PROCESSING_ERROR);
        }

        return new int[] {totalCount, successCount, failureCount};
    }

    /**
     * 엑셀에서 읽은 태그 문자열을 태그 이름 리스트로 변환합니다.
     * 예) "커피, 디저트, 브런치" -> ["커피", "디저트", "브런치"]
     */
    private List<String> parseTagNames(String tagsRaw) {
        if (tagsRaw == null || tagsRaw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(tagsRaw.split(","))
                .map(String::trim)
                .filter(tag -> !tag.isEmpty())
                .toList();
    }

    /**
     * 셀에서 문자열 값을 안전하게 가져옵니다.
     */
    private String getStringCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue();
            case NUMERIC -> String.valueOf(cell.getNumericCellValue());
            case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
            case FORMULA -> cell.getStringCellValue();
            default -> null;
        };
    }

    /**
     * 셀에서 Long 값을 안전하게 가져옵니다.
     */
    private Long getLongCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        return switch (cell.getCellType()) {
            case NUMERIC -> (long) cell.getNumericCellValue();
            case STRING -> {
                String value = cell.getStringCellValue();
                if (value == null || value.isBlank()) {
                    yield null;
                }
                yield Long.parseLong(value.trim());
            }
            default -> null;
        };
    }

    /**
     * CSV 한 줄을 콤마(,) 기준으로 파싱하되, 따옴표(") 안의 콤마는 값으로 취급합니다.
     * 간단한 용도의 경량 파서입니다.
     */
    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                result.add(trimQuotes(current.toString()));
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        // 마지막 컬럼 추가
        result.add(trimQuotes(current.toString()));

        return result;
    }

    private String trimQuotes(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private String safeGet(List<String> list, int index) {
        if (index < 0 || index >= list.size()) {
            return null;
        }
        return list.get(index);
    }

    private Long parseLongSafe(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * MIME 타입 & 확장자 검사
     * 
     * 파일의 MIME 타입과 확장자가 일치하고,
     * 허용된 형식인지 검증합니다.
     * 
     * @param contentType MIME 타입
     * @param fileName 파일명
     * @throws ValidationException 유효하지 않은 경우
     */
    private void verifyMimeType(String fileName) {
        log.info("verifyMimeType: fileName={}", fileName);
        // 파일 확장자 추출
        String fileExtension = FilenameUtils.getExtension(fileName).toLowerCase();
        log.info("fileExtension={}", fileExtension);
        log.info("isValidMimeType={}", isValidMimeType(fileExtension));
        if (!isValidMimeType(fileExtension)) {
            log.info("isValidMimeType=false");
            throw new CoreException(ErrorType.FileOperation.FILE_UPLOAD_INVALID_MIME_TYPE_ERROR);
        }
    }

    /**
     * 유효한 MIME 타입 및 확장자 체크
     * 
     * MediaType enum에 정의된 형식과 일치하는지 확인합니다.
     * 
     * @param contentType MIME 타입
     * @param fileExtension 파일 확장자
     * @return 유효하면 true, 아니면 false
     */
    private boolean isValidMimeType(String fileExtension) {
        log.info("isValidMimeType: fileExtension={}", fileExtension);
        for (String extension : List.of("xls", "xlsx", "csv")) {
            if (fileExtension.equals(extension)) {
                return true;
            }
        }
        return false;
    }
}

