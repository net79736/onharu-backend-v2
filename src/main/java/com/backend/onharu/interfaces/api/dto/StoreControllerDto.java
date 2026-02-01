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
            Double radius
    ) {
    }

    public record SearchStoresResponse(
            List<StoreResponse> stores
    ) {
    }

    public record GetStoreDetailResponse(
            StoreDetailResponse store
    ) {
        public GetStoreDetailResponse(Store store) {
            this(new StoreDetailResponse(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getLat(),
                store.getLng(),
                store.getImage(),
                store.getIntro(),
                store.getIntroduction(),
                store.getCategory().getId(),
                store.getIsOpen(),
                0.0,
                null,
                null,
                null
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

            @Schema(description = "이미지 경로", example = "https://onharu.com/images/store1.jpg")
            String image,

            @Schema(description = "한줄 소개", example = "따뜻한 한 끼 식사")
            String intro,

            @Schema(description = "가게 소개", example = "따뜻한 마음으로 환영합니다!")
            String introduction,

            @Schema(description = "카테고리 ID", example = "1")
            Long categoryId,

            @Schema(description = "영업중 여부", example = "true")
            Boolean isOpen,

            @Schema(description = "거리(km)", example = "1.5")
            Double distance
    ) {
        public StoreResponse(Store store) {
            this(
                store.getId(),
                store.getName(),
                store.getAddress(),
                store.getPhone(),
                store.getLat(),
                store.getLng(),
                store.getImage(),
                store.getIntro(),
                store.getIntroduction(),
                store.getCategory().getId(),
                store.getIsOpen(),
                0.0
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

            @Schema(description = "이미지 경로", example = "https://onharu.com/images/store1.jpg")
            String image,

            @Schema(description = "한줄 소개", example = "따뜻한 한 끼 식사")
            String intro,

            @Schema(description = "가게 소개", example = "따뜻한 마음으로 환영합니다!")
            String introduction,

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
            List<String> tags
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

        @Schema(description = "이미지 경로", example = "https://onharu.com/images/store1.jpg")
        String image,

        @Schema(description = "한줄 소개", example = "따뜻한 한 끼 식사", requiredMode = Schema.RequiredMode.REQUIRED)
        String intro,

        @Schema(description = "가게 소개", example = "따뜻한 마음으로 환영합니다!", requiredMode = Schema.RequiredMode.REQUIRED)
        String introduction,

        @Schema(description = "태그 목록", example = "[\"커피\", \"디저트\", \"브런치\"]")
        List<String> tagNames,

        @Schema(description = "영업시간 목록")
        List<BusinessHourRequest> businessHours
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

            @Schema(description = "이미지 경로", example = "https://onharu.com/images/store1.jpg")
            String image,

            @Schema(description = "전화번호", example = "0212345678", requiredMode = Schema.RequiredMode.REQUIRED)
            String phone,

            @Schema(description = "주소", example = "서울시 강남구 테헤란로 123", requiredMode = Schema.RequiredMode.REQUIRED)
            String address,

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
            List<BusinessHourRequest> businessHours
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
}
