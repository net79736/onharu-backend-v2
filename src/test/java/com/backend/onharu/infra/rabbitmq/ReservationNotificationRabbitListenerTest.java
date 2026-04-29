package com.backend.onharu.infra.rabbitmq;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.backend.onharu.event.listener.ReservationNotificationHistoryHandler;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReservationNotificationRabbitListener")
class ReservationNotificationRabbitListenerTest {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private ReservationNotificationHistoryHandler handler;

    @Mock
    private Channel channel;

    private ReservationNotificationRabbitListener listener;

    @BeforeEach
    void setUp() {
        listener = new ReservationNotificationRabbitListener(objectMapper, handler);
    }

    @Test
    void JSON이_깨져있으면_DLQ로_보내고_requeue하지_않는다() throws Exception {
        long deliveryTag = 11L;

        listener.onReservationNotification("{not json", channel, deliveryTag);

        verify(channel).basicNack(eq(deliveryTag), eq(false), eq(false));
        verify(channel, never()).basicAck(eq(deliveryTag), eq(false));
    }
}

