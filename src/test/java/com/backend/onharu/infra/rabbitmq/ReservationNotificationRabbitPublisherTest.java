package com.backend.onharu.infra.rabbitmq;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.backend.onharu.domain.common.enums.NotificationHistoryType;
import com.backend.onharu.event.model.ReservationEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationNotificationRabbitPublisher")
class ReservationNotificationRabbitPublisherTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    private ReservationNotificationRabbitPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new ReservationNotificationRabbitPublisher(rabbitTemplate, objectMapper);
        ReflectionTestUtils.setField(publisher, "reservationNotificationsQueue", "onharu.reservation.notifications");
    }

    @Test
    void 예약_알림을_JSON으로_직렬화해_큐로_publish한다() throws Exception {
        ReservationEvent event = new ReservationEvent(1L, 10L, 100L, NotificationHistoryType.RESERVATION_CREATED);

        publisher.publishReservationNotification(event);

        ArgumentCaptor<String> body = ArgumentCaptor.forClass(String.class);
        verify(rabbitTemplate).convertAndSend(eq(""), eq("onharu.reservation.notifications"), body.capture());

        JsonNode root = objectMapper.readTree(body.getValue());
        assertThat(root.path("type").asText()).isEqualTo("RESERVATION_NOTIFICATION");
        assertThat(root.path("reservationId").asLong()).isEqualTo(1L);
        assertThat(root.path("ownerId").asLong()).isEqualTo(10L);
        assertThat(root.path("childId").asLong()).isEqualTo(100L);
        assertThat(root.path("notificationType").asText()).isEqualTo("RESERVATION_CREATED");
        assertThat(((ObjectNode) root).hasNonNull("correlationId")).isTrue();
    }
}

