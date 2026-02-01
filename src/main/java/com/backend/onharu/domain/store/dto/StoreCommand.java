package com.backend.onharu.domain.store.dto;

import java.util.List;

import com.backend.onharu.interfaces.api.dto.StoreControllerDto.BusinessHourRequest;

public class StoreCommand {
    /**
     * 가게 생성 커맨드
     */
    public record CreateStoreCommand(
            Long ownerId,
            Long categoryId,
            String name,
            String address,
            String phone,
            String lat,
            String lng,
            String image,
            String introduction,
            String intro,
            List<String> tagNames,
            List<BusinessHourRequest> businessHours
    ) {
    }

    /**
     * 가게 수정 커맨드
     */
    public record UpdateStoreCommand(
            Long id,
            Long categoryId,
            String image,
            String phone,
            String address,
            String lat,
            String lng,
            String introduction,
            String intro,
            Boolean isOpen,
            List<String> tagNames,
            List<BusinessHourRequest> businessHours
    ) {
    }

    /**
     * 가게 삭제 커맨드
     */
    public record DeleteStoreCommand(
            Long id
    ) {
    }

    /**
     * 가게 영업 상태 변경 커맨드
     */
    public record ChangeOpenStatusCommand(
            Long id,
            Boolean isOpen
    ) {
    }
}
