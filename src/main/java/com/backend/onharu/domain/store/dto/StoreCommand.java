package com.backend.onharu.domain.store.dto;

import java.util.List;

import com.backend.onharu.domain.file.dto.FileCommand.ImageMetadata;
import com.backend.onharu.interfaces.api.dto.StoreControllerDto.BusinessHourRequest;

public class StoreCommand {

    /**
     * 가게 생성 커맨드
     * (이미지는 File 테이블에서 관리)
     */
    public record CreateStoreCommand(
            Long ownerId,
            Long categoryId,
            String name,
            String address,
            String phone,
            String lat,
            String lng,
            String introduction,
            String intro,
            List<String> tagNames,
            List<BusinessHourRequest> businessHours,
            List<ImageMetadata> images
    ) {
    }

    /**
     * 가게 수정 커맨드
     * (이미지는 File 테이블에서 관리. images가 넘어오면 기존 첨부를 삭제 후 새 목록으로 교체)
     */
    public record UpdateStoreCommand(
            Long id,
            Long categoryId,
            String phone,
            String address,
            String lat,
            String lng,
            String introduction,
            String intro,
            Boolean isOpen,
            Boolean isSharing,
            List<String> tagNames,
            List<BusinessHourRequest> businessHours,
            List<ImageMetadata> images
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
