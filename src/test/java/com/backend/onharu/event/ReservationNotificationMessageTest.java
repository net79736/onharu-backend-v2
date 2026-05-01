package com.backend.onharu.event;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.infra.db.child.ChildJpaRepository;
import com.backend.onharu.infra.db.favorite.FavoriteJpaRepository;
import com.backend.onharu.infra.db.file.FileJpaRepository;
import com.backend.onharu.infra.db.level.LevelJpaRepository;
import com.backend.onharu.infra.db.owner.OwnerJpaRepository;
import com.backend.onharu.infra.db.reservation.ReservationJpaRepository;
import com.backend.onharu.infra.db.store.CategoryJpaRepository;
import com.backend.onharu.infra.db.store.StoreJpaRepository;
import com.backend.onharu.infra.db.storeschedule.StoreScheduleJpaRepository;
import com.backend.onharu.infra.db.tag.TagJpaRepository;
import com.backend.onharu.infra.db.user.UserJpaRepository;

/**
 * 예약 알림 메시지 enum의 테스트입니다.
 *
 * 사장(Owner)용·아동(Child)용 메시지가 각 이벤트 타입별로 올바르게 반환되는지 검증합니다.
 */
@DisplayName("ReservationNotificationMessage 단위 테스트")
class ReservationNotificationMessageTest {

    @Autowired
    private ReservationJpaRepository reservationJpaRepository;

    @Autowired
    private ChildJpaRepository childJpaRepository;

    @Autowired
    private UserJpaRepository userJpaRepository;

    @Autowired
    private StoreScheduleJpaRepository storeScheduleJpaRepository;

    @Autowired
    private StoreJpaRepository storeJpaRepository;

    @Autowired
    private OwnerJpaRepository ownerJpaRepository;

    @Autowired
    private CategoryJpaRepository categoryJpaRepository;

    @Autowired
    private LevelJpaRepository levelJpaRepository;
    
    @Autowired
    private FileJpaRepository fileJpaRepository;
    
    @Autowired
    private FavoriteJpaRepository favoriteJpaRepository;
    
    @Autowired
    private TagJpaRepository tagJpaRepository;

    @Nested
    @DisplayName("NotificationHistoryType 매핑 테스트")
    class FromTest {

        @ParameterizedTest
        @EnumSource(NotificationHistoryType.class)
        @DisplayName("NotificationHistoryType과 enum이 1:1 매핑되어야 한다")
        void shouldMapNotificationHistoryTypeToEnum(NotificationHistoryType type) {
            // when
            ReservationNotificationMessage message = ReservationNotificationMessage.from(type);

            // then
            assertThat(message).isNotNull();
            assertThat(message.name()).isEqualTo(type.name());
        }
    }

    @Nested
    @DisplayName("사장(Owner) 메시지 테스트")
    class OwnerMessageTest {

        @Test
        @DisplayName("RESERVATION_CREATED - 예약 번호가 포맷팅되어 사장용 메시지 반환")
        void shouldReturnOwnerMessageWithReservationId_WhenReservationCreated() {
            // given
            Long reservationId = 12345L;
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_CREATED;

            // when
            String ownerMessage = message.getOwnerMessageTemplate("", reservationId);

            // then
            assertThat(ownerMessage).isEqualTo("새로운 예약이 확정되었습니다. 예약 번호: 12345");
        }

        @Test
        @DisplayName("RESERVATION_CREATED - reservationId가 null이면 0으로 치환")
        void shouldReturnOwnerMessageWithZero_WhenReservationIdIsNull() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_CREATED;

            // when
            String ownerMessage = message.getOwnerMessageTemplate("", null);

            // then
            assertThat(ownerMessage).isEqualTo("새로운 예약이 확정되었습니다. 예약 번호: 0");
        }

        @Test
        @DisplayName("RESERVATION_CREATED - 가게명이 있으면 예약 번호 포맷 후에도 [가게명] 접두")
        void shouldPrefixStoreName_WhenReservationCreatedWithStoreName() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_CREATED;

            // when
            String ownerMessage = message.getOwnerMessageTemplate("테스트카페", 99L);

            // then
            assertThat(ownerMessage).isEqualTo("[테스트카페] 새로운 예약이 확정되었습니다. 예약 번호: 99");
        }

        @Test
        @DisplayName("RESERVATION_CONFIRMED - 사장용 메시지 반환")
        void shouldReturnOwnerMessage_WhenReservationConfirmed() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_CONFIRMED;

            // when
            String ownerMessage = message.getOwnerMessageTemplate("", 1L);

            // then
            assertThat(ownerMessage).isEqualTo("예약이 확정되었습니다. 매장에서 만나요.");
        }

        @Test
        @DisplayName("RESERVATION_CANCELED - 사장용 메시지 반환")
        void shouldReturnOwnerMessage_WhenReservationCanceled() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_CANCELED;

            // when
            String ownerMessage = message.getOwnerMessageTemplate("", 1L);

            // then
            assertThat(ownerMessage).isEqualTo("예약이 취소되었습니다.");
        }

        @Test
        @DisplayName("RESERVATION_COMPLETED - 사장용 메시지 반환")
        void shouldReturnOwnerMessage_WhenReservationCompleted() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_COMPLETED;

            // when
            String ownerMessage = message.getOwnerMessageTemplate("", 1L);

            // then
            assertThat(ownerMessage).isEqualTo("예약이 완료됐어요! 매장에서 만나요.");
        }

        @Test
        @DisplayName("RESERVATION_REJECTED - 사장용 메시지 반환")
        void shouldReturnOwnerMessage_WhenReservationRejected() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_REJECTED;

            // when
            String ownerMessage = message.getOwnerMessageTemplate("", 1L);

            // then
            assertThat(ownerMessage).isEqualTo("예약이 거절되었습니다.");
        }
    }

    @Nested
    @DisplayName("아동(Child) 메시지 테스트")
    class ChildMessageTest {

        @Test
        @DisplayName("RESERVATION_CREATED - 아동용 메시지 없음(null)")
        void shouldReturnNull_WhenReservationCreated() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_CREATED;

            // when
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(childMessage).isNull();
        }

        @Test
        @DisplayName("RESERVATION_CONFIRMED - 아동용 메시지 없음(null)")
        void shouldReturnNull_WhenReservationConfirmed() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_CONFIRMED;

            // when
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(childMessage).isNull();
        }

        @Test
        @DisplayName("RESERVATION_CANCELED - 아동용 메시지 반환")
        void shouldReturnChildMessage_WhenReservationCanceled() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_CANCELED;

            // when
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(childMessage).isEqualTo("예약 취소가 잘 처리됐어요. 다음에 또 봐요!");
        }

        @Test
        @DisplayName("RESERVATION_COMPLETED - 아동용 메시지 반환")
        void shouldReturnChildMessage_WhenReservationCompleted() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_COMPLETED;

            // when
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(childMessage).isEqualTo("예약이 완료됐어요! 매장에서 만나요.");
        }

        @Test
        @DisplayName("RESERVATION_REJECTED - 아동용 메시지 반환 (사장용과 상이한 친근한 표현)")
        void shouldReturnChildMessage_WhenReservationRejected() {
            // given
            ReservationNotificationMessage message = ReservationNotificationMessage.RESERVATION_REJECTED;

            // when
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(childMessage).isEqualTo("미안해요, 이번 예약은 사정상 어려워졌어요.");
        }
    }

    @Nested
    @DisplayName("전체 프로세스 시나리오 테스트")
    class FullProcessScenarioTest {

        @Test
        @DisplayName("예약 생성 시 - 사장에게만 메시지 전달, 아동 메시지 없음")
        void shouldDeliverOwnerMessageOnly_WhenReservationCreated() {
            // given
            NotificationHistoryType type = NotificationHistoryType.RESERVATION_CREATED;
            Long reservationId = 100L;

            // when
            ReservationNotificationMessage message = ReservationNotificationMessage.from(type);
            String ownerMessage = message.getOwnerMessageTemplate("", reservationId);
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(ownerMessage).isEqualTo("새로운 예약이 확정되었습니다. 예약 번호: 100");
            assertThat(childMessage).isNull();
        }

        @Test
        @DisplayName("예약 취소 시 - 사장·아동 각각 다른 메시지 전달")
        void shouldDeliverDistinctMessages_WhenReservationCanceled() {
            // given
            NotificationHistoryType type = NotificationHistoryType.RESERVATION_CANCELED;

            // when
            ReservationNotificationMessage message = ReservationNotificationMessage.from(type);
            String ownerMessage = message.getOwnerMessageTemplate("", 1L);
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(ownerMessage).isEqualTo("예약이 취소되었습니다.");
            assertThat(childMessage).isEqualTo("예약 취소가 잘 처리됐어요. 다음에 또 봐요!");
            assertThat(ownerMessage).isNotEqualTo(childMessage);
        }

        @Test
        @DisplayName("예약 거절 시 - 사장·아동 각각 다른 메시지 전달")
        void shouldDeliverDistinctMessages_WhenReservationRejected() {
            // given
            NotificationHistoryType type = NotificationHistoryType.RESERVATION_REJECTED;

            // when
            ReservationNotificationMessage message = ReservationNotificationMessage.from(type);
            String ownerMessage = message.getOwnerMessageTemplate("", 1L);
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(ownerMessage).isEqualTo("예약이 거절되었습니다.");
            assertThat(childMessage).isEqualTo("미안해요, 이번 예약은 사정상 어려워졌어요.");
            assertThat(ownerMessage).isNotEqualTo(childMessage);
        }

        @Test
        @DisplayName("예약 완료 시 - 사장·아동 동일 메시지 전달")
        void shouldDeliverSameMessage_WhenReservationCompleted() {
            // given
            NotificationHistoryType type = NotificationHistoryType.RESERVATION_COMPLETED;

            // when
            ReservationNotificationMessage message = ReservationNotificationMessage.from(type);
            String ownerMessage = message.getOwnerMessageTemplate("", 1L);
            String childMessage = message.getChildMessageTemplate();

            // then
            assertThat(ownerMessage).isEqualTo("예약이 완료됐어요! 매장에서 만나요.");
            assertThat(childMessage).isEqualTo("예약이 완료됐어요! 매장에서 만나요.");
            assertThat(ownerMessage).isEqualTo(childMessage);
        }
    }
}
