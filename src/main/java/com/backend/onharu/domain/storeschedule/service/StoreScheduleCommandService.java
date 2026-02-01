package com.backend.onharu.domain.storeschedule.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.backend.onharu.domain.store.model.Store;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.CreateStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.DeleteStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleCommand.UpdateStoreScheduleCommand;
import com.backend.onharu.domain.storeschedule.dto.StoreScheduleRepositroyParam.GetStoreScheduleByIdParam;
import com.backend.onharu.domain.storeschedule.model.StoreSchedule;
import com.backend.onharu.domain.storeschedule.repository.StoreScheduleRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class StoreScheduleCommandService {
    private final StoreScheduleRepository storeScheduleRepository;

    /**
     * 가게 일정 생성
     */
    public StoreSchedule createStoreSchedule(CreateStoreScheduleCommand command, Store store) {
        StoreSchedule storeSchedule = StoreSchedule.builder()
                .store(store)
                .scheduleDate(command.scheduleDate())
                .startTime(command.startTime())
                .endTime(command.endTime())
                .maxPeople(command.maxPeople())
                .build();

        return storeScheduleRepository.save(storeSchedule);
    }

    /**
     * 가게 일정 생성 (Batch Save)
     * @param schedules
     */
    public List<StoreSchedule> createStoreSchedules(List<StoreSchedule> schedules) {
        return storeScheduleRepository.saveAll(schedules);
    }

    /**
     * 가게 일정 정보 수정
     */
    public void updateStoreSchedule(UpdateStoreScheduleCommand command) {
        StoreSchedule storeSchedule = storeScheduleRepository.getStoreScheduleById(
                new GetStoreScheduleByIdParam(command.id()));
        
        storeSchedule.update(
                command.scheduleDate(),
                command.startTime(),
                command.endTime(),
                command.maxPeople()
        );
    }

    /**
     * 가게 일정 삭제
     */
    public void deleteStoreSchedule(DeleteStoreScheduleCommand command) {
        StoreSchedule storeSchedule = storeScheduleRepository.getStoreScheduleById(
                new GetStoreScheduleByIdParam(command.id()));
        
        storeScheduleRepository.delete(storeSchedule);
    }
}
