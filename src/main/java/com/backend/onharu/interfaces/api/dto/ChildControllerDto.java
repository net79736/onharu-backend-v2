package com.backend.onharu.interfaces.api.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.backend.onharu.domain.common.enums.ResrvationType;
import com.backend.onharu.domain.common.enums.StatusType;

import io.swagger.v3.oas.annotations.media.Schema;

public class ChildControllerDto {

    public record GetChildProfileResponse(
            ChildProfileResponse child
    ) {
    }

    public record UpdateChildProfileRequest(
            @Schema(description = "닉네임", example = "온하루친구")
            String nickname
    ) {
    }

    public record GetChildReservationsResponse(
            List<ReservationResponse> reservations
    ) {
    }

    public record ChildProfileResponse(
            @Schema(description = "아이 ID", example = "1")
            Long id,

            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "이름", example = "김길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "계정 상태", example = "ACTIVE", allowableValues = {"PENDING", "ACTIVE", "LOCKED", "DELETED", "BLOCKED"})
            StatusType statusType,

            @Schema(description = "증명서 파일 경로", example = "/certificates/certificate.pdf")
            String certificate,

            @Schema(description = "지원 대상 승인 여부", example = "true")
            Boolean isVerified
    ) {
    }

    public record ReservationResponse(
            @Schema(description = "예약 ID", example = "1")
            Long id,

            @Schema(description = "아동 ID", example = "1")
            Long childId,

            @Schema(description = "예약 가능 일정 ID", example = "1")
            Long availableScheduleId,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "가게 이름", example = "따뜻한 식당")
            String storeName,

            @Schema(description = "예약 상태", example = "CONFIRMED", allowableValues = "WAITING, CONFIRMED, CANCELED, COMPLETED")
            ResrvationType reservationType,

            @Schema(description = "예약일시", example = "2024-12-31T23:59:59")
            LocalDateTime reservationAt
    ) {
    }

    // 카드 관련 DTO는 SQL 스키마에 카드 테이블이 없으므로 주석 처리
    // 필요시 별도 카드 테이블 추가 후 활성화
    public record IssueCardRequest(
            @Schema(description = "카드 번호", example = "1234-5678-9012-3456")
            String cardNumber
    ) {
    }

    public record IssueCardResponse(
            @Schema(description = "카드 ID", example = "1")
            Long cardId
    ) {
    }

    public record UpdateCardRequest(
            @Schema(description = "카드 번호", example = "1234-5678-9012-3456")
            String cardNumber
    ) {
    }

    public record GetCardResponse(
            CardResponse card
    ) {
    }

    public record CardResponse(
            @Schema(description = "카드 ID", example = "1")
            Long id,

            @Schema(description = "카드 번호", example = "1234-5678-9012-3456")
            String cardNumber
    ) {
    }

    public record UpdateCertificateRequest(
            @Schema(description = "닉네임", example = "온하루친구")
            String nickname
    ) {
    }

    public record GetCertificateResponse(
            CertificateResponse certificate
    ) {
    }

    public record CertificateResponse(
            @Schema(description = "증명서 ID", example = "1")
            Long id,

            @Schema(description = "사용자 ID", example = "1")
            Long userId,

            @Schema(description = "아동 이름", example = "김길동")
            String name,

            @Schema(description = "증명서 파일 경로", example = "/certificates/certificate.pdf")
            String certificate,

            @Schema(description = "지원 대상 승인 여부", example = "true")
            Boolean isVerified
    ) {
    }

    public record GetLikedStoreListResponse(
            List<LikedStoreResponse> stores
    ) {
    }

    public record LikedStoreResponse(
            @Schema(description = "관심 가게 ID", example = "1")
            Long id,

            @Schema(description = "가게 ID", example = "1")
            Long storeId,

            @Schema(description = "가게 이름", example = "따뜻한 식당")
            String storeName,

            @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
            String address,

            @Schema(description = "전화번호", example = "0212345678")
            String phone,

            @Schema(description = "이미지 경로", example = "/images/store1.jpg")
            String image
    ) {
    }

    /**
     * 아동 회원가입 요청 DTO
     * 사용자 정보와 증명서 파일을 함께 받습니다.
     */
    public record SignUpChildRequest(
            @Schema(description = "아동 사용자 ID", example = "child123")
            String userId,

            @Schema(description = "비밀번호", example = "password123!")
            String password,

            @Schema(description = "비밀번호 확인", example = "password123!")
            String passwordConfirm,

            @Schema(description = "이름", example = "홍길동")
            String name,

            @Schema(description = "전화번호", example = "01012345678")
            String phone,

            @Schema(description = "아동 증명서 파일 경로", example = "/certificates/certificate.pdf")
            String certificate
    ) {
    }

    /**
     * 아동 회원가입 응답 DTO
     */
    public record SignUpChildResponse(
            @Schema(description = "ID", example = "1")
            Long id,

            @Schema(description = "아동 사용자 ID", example = "child123")
            String userId
    ) {
    }
}
