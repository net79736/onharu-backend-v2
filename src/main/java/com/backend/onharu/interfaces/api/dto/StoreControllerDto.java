package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import com.backend.onharu.domain.store.model.Category;
import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.StoreScheduleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

public class StoreControllerDto {

    @Schema(description = "가게 목록 조회 요청")
    public record SearchStoresRequest(
            @Schema(description = "위도", example = "37.5665")
            Double latitude,

            @Schema(description = "경도", example = "126.9780")
            Double longitude,

            @Schema(description = "반경(km)", example = "5.0")
            Double radius,

            @Schema(description = "페이지 번호 (1부터 시작)", example = "1")
            Integer pageNum,

            @Schema(description = "페이지당 항목 수", example = "10")
            Integer perPage,

            @Schema(description = "정렬 기준", example = "id")
            String sortField,

            @Schema(description = "정렬 방향", example = "desc")
            String sortDirection
    ) {
        /**
         * 기본값 설정: pageNum이 null이거나 0 이하면 1, perPage가 null이거나 0 이하면 10
         */
        public Integer pageNum() {
            return pageNum != null && pageNum > 0 ? pageNum : 1;
        }

        public Integer perPage() {
            return perPage != null && perPage > 0 ? perPage : 10;
        }
    }

    public record SearchStoresResponse(
            @Schema(description = "가게 목록")
            List<StoreResponse> stores,
            
            @Schema(description = "전체 가게 개수")
            Long totalCount,
            
            @Schema(description = "현재 페이지 번호")
            Integer currentPage,
            
            @Schema(description = "전체 페이지 수")
            Integer totalPages,
            
            @Schema(description = "페이지당 항목 수")
            Integer perPage
    ) {
    }

    public record GetStoreDetailResponse(
            StoreDetailResponse store
    ) {

        /**
         * 첨부 이미지 목록으로 상세 응답 생성
         */
        public GetStoreDetailResponse(Store store, List<String> images) {
            this(new StoreDetailResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getLat(),
                store.getLng(),
                store.getIntroduction(),
                store.getIntro(),
                store.getCategory().getId(),
                store.getIsOpen(),
                0.0,
                null,
                null,
                null,
                resolveImages(images)
            ));
        }
    }

    public record StoreResponse(
            @Schema(description = "가게 ID", example = "1")
            Long id,

            @Schema(description = "가게 이름", example = "따뜻한 식당")
            String name,

            @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
            String address,

            @Schema(description = "전화번호", example = "0212345678")
            String phone,

            @Schema(description = "위도", example = "37.5665")
            String lat,

            @Schema(description = "경도", example = "126.9780")
            String lng,

            @Schema(description = "가게 소개", example = "따뜻한 마음으로 환영합니다!")
            String introduction,

            @Schema(description = "한줄 소개", example = "따뜻한 한 끼 식사")
            String intro,

            @Schema(description = "카테고리 ID", example = "1")
            Long categoryId,

            @Schema(description = "카테고리 이름", example = "카페")
            String categoryName,

            @Schema(description = "영업중 여부", example = "true")
            Boolean isOpen,

            @Schema(description = "거리(km)", example = "1.5")
            Double distance,

            @Schema(description = "첨부 이미지 URL 목록 (표시 순서대로)")
            List<String> images
    ) {
        public StoreResponse(Store store) {
            this(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getLat(),
                store.getLng(),
                store.getIntroduction(),
                store.getIntro(),
                store.getCategory().getId(),
                store.getCategory().getName(),
                store.getIsOpen(),
                0.0,
                List.of() // 기본 생성자는 빈 리스트, 이미지 목록은 별도로 설정
            );
        }

        /**
         * 이미지 목록과 함께 StoreResponse 생성
         */
        public StoreResponse(Store store, List<String> images) {
            this(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getLat(),
                store.getLng(),
                store.getIntroduction(),
                store.getIntro(),
                store.getCategory().getId(),
                store.getCategory().getName(),
                store.getIsOpen(),
                0.0,
                resolveImages(images)
            );
        }
    }

    public record StoreDetailResponse(
            @Schema(description = "가게 ID", example = "1")
            Long id,

            @Schema(description = "가게 이름", example = "따뜻한 식당")
            String name,

            @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
            String address,

            @Schema(description = "전화번호", example = "0212345678")
            String phone,

            @Schema(description = "위도", example = "37.5665")
            String lat,

            @Schema(description = "경도", example = "126.9780")
            String lng,

            @Schema(description = "가게 소개", example = "따뜻한 마음으로 환영합니다!")
            String introduction,

            @Schema(description = "한줄 소개", example = "따뜻한 한 끼 식사")
            String intro,

            @Schema(description = "카테고리 ID", example = "1")
            Long categoryId,

            @Schema(description = "영업중 여부", example = "true")
            Boolean isOpen,

            @Schema(description = "거리(km)", example = "1.5")
            Double distance,

            @Schema(description = "영업시간 목록")
            List<BusinessHourResponse> businessHours,

            @Schema(description = "예약 가능 일정 목록")
            List<StoreScheduleResponse> storeSchedules,

            @Schema(description = "태그 목록")
            List<String> tags,

            @Schema(description = "첨부 이미지 URL 목록 (다중 이미지, 표시 순서대로). 기존 단일 image 대체.")
            List<String> images
    ) {
    }

    public record BusinessHourResponse(
            @Schema(description = "영업일", example = "MON, TUE, WED, THU, FRI, SAT, SUN", allowableValues = "MON, TUE, WED, THU, FRI, SAT, SUN")
            List<LocalDate> businessDays,

            @Schema(description = "오픈 시간", example = "09:00")
            LocalTime openTime,

            @Schema(description = "마감 시간", example = "22:00")
            LocalTime closeTime
    ) {
    }

    /**
     * 이미 업로드된 이미지 메타데이터 (Presigned URL로 MinIO 업로드 완료 후 클라이언트가 보관한 정보)
     */
    @Schema(description = "이미 업로드된 이미지 메타데이터")
    public record ImageMetadataRequest(
            @Schema(description = "S3 객체 키 (Presigned URL 업로드 시 사용한 경로)", example = "image/uuid-photo.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
            String fileKey,

            @Schema(description = "다운로드 URL 또는 파일 경로", example = "https://minio.example.com/bucket/image/uuid-photo.jpg", requiredMode = Schema.RequiredMode.REQUIRED)
            String filePath,

            @Schema(description = "표시 순서 (0이 대표 이미지)", example = "0")
            Integer displayOrder
    ) {
    }

    @Schema(description = "가게 정보 생성 요청")
    public record OpenStoreRequest(
        @Schema(description = "카테고리 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long categoryId,

        @Schema(description = "가게 이름", example = "따뜻한 식당", requiredMode = Schema.RequiredMode.REQUIRED)
        String name,

        @Schema(description = "주소", example = "서울시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
        String address,

        @Schema(description = "전화번호", example = "0212345678", requiredMode = Schema.RequiredMode.REQUIRED)
        String phone,

        @Schema(description = "위도", example = "37.5665", requiredMode = Schema.RequiredMode.REQUIRED)
        String lat,

        @Schema(description = "경도", example = "126.9780", requiredMode = Schema.RequiredMode.REQUIRED)
        String lng,

        @Schema(description = "가게 소개", example = "따뜻한 마음으로 환영합니다!", requiredMode = Schema.RequiredMode.REQUIRED)
        String introduction,

        @Schema(description = "한줄 소개", example = "따뜻한 한 끼 식사", requiredMode = Schema.RequiredMode.REQUIRED)
        String intro,

        @Schema(description = "태그 목록", example = "[\"커피\", \"디저트\", \"브런치\"]")
        List<String> tagNames,

        @Schema(description = "영업시간 목록")
        List<BusinessHourRequest> businessHours,

        @Schema(description = "이미 업로드된 이미지 목록 (Presigned URL 업로드 완료 후 fileKey, filePath만 보관하여 전달)")
        List<ImageMetadataRequest> images
    ) {
    }

    @Schema(description = "영업시간 요청")
    public record BusinessHourRequest(
            @Schema(description = "영업일", example = "2024-12-31", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalDate businessDay,

            @Schema(description = "오픈 시간", example = "09:00", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalTime openTime,

            @Schema(description = "마감 시간", example = "22:00", requiredMode = Schema.RequiredMode.REQUIRED)
            LocalTime closeTime
    ) {
    }

    public record OpenStoreResponse(
            @Schema(description = "가게 ID", example = "1")
            Long id
    ) {
    }

    @Schema(description = "가게 정보 수정 요청")
    public record UpdateStoreRequest(
            @Schema(description = "카테고리 ID", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
            Long categoryId,

            @Schema(description = "주소", example = "서울시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
            String address,

            @Schema(description = "전화번호", example = "0212345678", requiredMode = Schema.RequiredMode.REQUIRED)
            String phone,

            @Schema(description = "위도", example = "37.5665", requiredMode = Schema.RequiredMode.REQUIRED)
            String lat,

            @Schema(description = "경도", example = "126.9780", requiredMode = Schema.RequiredMode.REQUIRED)
            String lng,

            @Schema(description = "소개글", example = "따뜻한 한 끼 식사", requiredMode = Schema.RequiredMode.REQUIRED)
            String introduction,

            @Schema(description = "한 줄 소개", example = "따뜻한 마음으로 환영합니다!", requiredMode = Schema.RequiredMode.REQUIRED)
            String intro,

            @Schema(description = "영업 여부", example = "true")
            Boolean isOpen,

            @Schema(description = "태그 목록", example = "[\"커피\", \"디저트\", \"브런치\"]")
            List<String> tagNames,

            @Schema(description = "영업시간 목록")
            List<BusinessHourRequest> businessHours,

            @Schema(description = "이미 업로드된 이미지 목록 (넘기면 기존 첨부 삭제 후 이 목록으로 교체)")
            List<ImageMetadataRequest> images
    ) {
    }

    public record CategoryResponse(
            @Schema(description = "카테고리 ID", example = "1")
            Long id,

            @Schema(description = "카테고리 이름", example = "카페")
            String name
    ) {
        public CategoryResponse(Category category) {
            this(
                category.getId(),
                category.getName()
            );
        }
    }

    /**
     * 첨부 이미지 목록을 해석하여 반환 (이미지는 File 테이블에서만 관리)
     */
    private static List<String> resolveImages(List<String> images) {
        return (images != null && !images.isEmpty()) ? images : List.of();
    }
}
