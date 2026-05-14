package com.flowboard.card_service;

import com.flowboard.card_service.dto.BulkNotificationRequestDto;
import com.flowboard.card_service.dto.NotificationRequestDto;
import com.flowboard.card_service.service.impl.NotificationProducerImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

class NotificationProducerImplTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private NotificationProducerImpl producer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        producer = new NotificationProducerImpl(rabbitTemplate);
        ReflectionTestUtils.setField(producer, "exchange", "exchange");
        ReflectionTestUtils.setField(producer, "singleRoutingKey", "single");
        ReflectionTestUtils.setField(producer, "bulkRoutingKey", "bulk");
    }

    @Test
    void sendBulk_whenDisabled_skipsPublish() {
        ReflectionTestUtils.setField(producer, "rabbitmqEnabled", false);
        producer.sendBulk(new BulkNotificationRequestDto());
    }

    @Test
    void sendBulk_whenEnabled_publishesMessage() {
        ReflectionTestUtils.setField(producer, "rabbitmqEnabled", true);
        BulkNotificationRequestDto dto = new BulkNotificationRequestDto();

        producer.sendBulk(dto);

        verify(rabbitTemplate).convertAndSend("exchange", "bulk", dto);
    }

    @Test
    void sendBulk_whenPublishFails_swallowsException() {
        ReflectionTestUtils.setField(producer, "rabbitmqEnabled", true);
        BulkNotificationRequestDto dto = new BulkNotificationRequestDto();
        doThrow(new RuntimeException("boom")).when(rabbitTemplate).convertAndSend("exchange", "bulk", dto);

        producer.sendBulk(dto);
    }

    @Test
    void sendSingle_whenDisabled_skipsPublish() {
        ReflectionTestUtils.setField(producer, "rabbitmqEnabled", false);
        producer.sendSingle(new NotificationRequestDto());
    }

    @Test
    void sendSingle_whenEnabled_publishesMessage() {
        ReflectionTestUtils.setField(producer, "rabbitmqEnabled", true);
        NotificationRequestDto dto = new NotificationRequestDto();

        producer.sendSingle(dto);

        verify(rabbitTemplate).convertAndSend("exchange", "single", dto);
    }

    @Test
    void sendSingle_whenPublishFails_swallowsException() {
        ReflectionTestUtils.setField(producer, "rabbitmqEnabled", true);
        NotificationRequestDto dto = new NotificationRequestDto();
        doThrow(new RuntimeException("boom")).when(rabbitTemplate).convertAndSend("exchange", "single", dto);

        producer.sendSingle(dto);
    }
}
