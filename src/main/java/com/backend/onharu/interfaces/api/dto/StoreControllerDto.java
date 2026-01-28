package com.backend.onharu.interfaces.api.dto;

import java.time.LocalTime;
import java.util.List;

import com.backend.onharu.domain.common.enums.DayType;
import com.backend.onharu.interfaces.api.dto.StoreScheduleControllerDto.AvailableScheduleResponse;

import io.swagger.v3.oas.annotations.media.Schema;

public class StoreControllerDto {

    public record GetStoreListResponse(
            List<StoreResponse> stores
    ) {
    }

    public record GetStoreDetailResponse(
            StoreDetailResponse store
    ) {
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

            @Schema(description = "이미지 경로", example = "/images/store1.jpg")
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

            @Schema(description = "이미지 경로", example = "/images/store1.jpg")
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
            List<AvailableScheduleResponse> availableSchedules,

            @Schema(description = "태그 목록")
            List<String> tags
    ) {
    }

    public record BusinessHourResponse(
            @Schema(description = "영업일", example = "MON, TUE, WED, THU, FRI, SAT, SUN", allowableValues = "MON, TUE, WED, THU, FRI, SAT, SUN")
            List<DayType> businessDays,

            @Schema(description = "오픈 시간", example = "09:00")
            LocalTime openTime,

            @Schema(description = "마감 시간", example = "22:00")
            LocalTime closeTime
    ) {
    }

    public record OpenStoreRequest(
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

        @Schema(description = "이미지 경로", example = "/images/store1.jpg")
        String image,

        @Schema(description = "한줄 소개", example = "따뜻한 한 끼 식사")
        String intro,

        @Schema(description = "가게 소개", example = "따뜻한 마음으로 환영합니다!")
        String introduction
    ) {
    }

    public record BusinessHourRequest(
            @Schema(description = "영업일", example = "MON", allowableValues = "MON, TUE, WED, THU, FRI, SAT, SUN")
            DayType businessDay,

            @Schema(description = "오픈 시간", example = "09:00")
            LocalTime openTime,

            @Schema(description = "마감 시간", example = "22:00")
            LocalTime closeTime
    ) {
    }

    public record OpenStoreResponse(
            @Schema(description = "가게 ID", example = "1")
            Long id
    ) {
    }

    public record UpdateStoreRequest(
            @Schema(description = "카테고리 ID", example = "1")
            Long categoryId,

            @Schema(description = "이미지 경로", example = "/images/store1.jpg")
            String image,

            @Schema(description = "전화번호", example = "0212345678")
            String phone,

            @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
            String address,

            @Schema(description = "위도", example = "37.5665")
            String lat,

            @Schema(description = "경도", example = "126.9780")
            String lng,

            @Schema(description = "소개글", example = "따뜻한 한 끼 식사")
            String introduction,

            @Schema(description = "한 줄 소개", example = "따뜻한 마음으로 환영합니다!")
            String intro,

            @Schema(description = "영업 여부", example = "true")
            Boolean isOpen,

            @Schema(description = "영업시간 목록")
            List<BusinessHourRequest> businessHours
    ) {
    }
}
