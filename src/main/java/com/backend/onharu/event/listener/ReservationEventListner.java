package com.backend.onharu.event.listener;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.backend.onharu.domain.child.dto.ChildRepositoryParam.GetChildByIdParam;
import com.backend.onharu.domain.child.model.Child;
import com.backend.onharu.domain.child.repository.ChildRepository;
import com.backend.onharu.domain.notification.model.NotificationHistory;
import com.backend.onharu.domain.notification.repository.NotificationHistoryRepository;
import com.backend.onharu.domain.owner.dto.OwnerRepositoryParam.GetOwnerByIdParam;
import com.backend.onharu.domain.owner.model.Owner;
import com.backend.onharu.domain.owner.repository.OwnerRepository;
import com.backend.onharu.domain.reservation.model.Reservation;
import com.backend.onharu.event.ReservationNotificationMessage;
import com.backend.onharu.event.model.ReservationEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReservationEventListner {

    private final NotificationHistoryRepository notificationHistoryRepository;

    private final OwnerRepository ownerRepository;

    private final ChildRepository childRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleReservationEvent(ReservationEvent event) {
        log.info("Current Thread: {}", Thread.currentThread().getName());
        log.info("Notification process start. reservationId : {}, type : {}", event.reservationId(), event.type());

        // 이벤트 타입에 따라 메시지 반환 (가게 사장용·아동용)
        ReservationNotificationMessage msg = ReservationNotificationMessage.from(event.type());

        String ownerMessage = msg.getOwnerMessage(event.reservationId());
        if (isNotEmpty(ownerMessage)) {
            log.info("Owner notification saved. reservationId : {}, ownerId : {}, ownerMessage : {}", event.reservationId(), event.ownerId(), ownerMessage);
            Owner owner = ownerRepository.getOwnerById(new GetOwnerByIdParam(event.ownerId()));
            notificationHistoryRepository.save(NotificationHistory.builder()
                .user(owner.getUser())
                .type(event.type())
                .title("매장 관리 알림")
                .message(ownerMessage)
                .relatedEntityType(Reservation.class.getSimpleName())
                .relatedEntityId(event.reservationId())
                .isRead(false)
                .build());
        }

        String childMessage = msg.getChildMessage();
        if (isNotEmpty(childMessage)) {
            log.info("Child notification saved. reservationId : {}, childId : {}, childMessage : {}", event.reservationId(), event.childId(), childMessage);
            Child child = childRepository.getChildById(new GetChildByIdParam(event.childId()));
            notificationHistoryRepository.save(NotificationHistory.builder()
                .user(child.getUser())
                .type(event.type())
                .title("예약 안내 소식")
                .message(childMessage)
                .relatedEntityType(Reservation.class.getSimpleName())
                .relatedEntityId(event.reservationId())
                .isRead(false)
                .build());
        }
    }
}
