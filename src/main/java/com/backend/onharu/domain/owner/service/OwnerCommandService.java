package com.backend.onharu.domain.owner.service;

import com.backend.onharu.domain.owner.dto.OwnerCommand.CreateOwnerCommand;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.repository.OwnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사업자 Command Service
 * <p>
 * 사업자 도메인의 상태를 변경하는 비즈니스 로직을 처리하는 서비스입니다.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class OwnerCommandService {

    private final OwnerRepository ownerRepository;

    /**
     * 사업자를 생성합니다.
     *
     * @param command 사업자 생성 Command
     * @return 생성된 사업자 엔티티
     */
    public Owner createOwner(CreateOwnerCommand command) {
        Owner owner = Owner.builder()
                .user(command.user())
                .level(command.level())
                .businessNumber(command.businessNumber())
                .build();

        return ownerRepository.save(owner);
    }
}
