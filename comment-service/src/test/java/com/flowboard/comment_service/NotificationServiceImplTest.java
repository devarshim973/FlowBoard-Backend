package com.flowboard.comment_service;

import com.flowboard.comment_service.client.CardClient;
import com.flowboard.comment_service.client.UserClient;
import com.flowboard.comment_service.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class NotificationServiceImplTest {

    @Mock
    private UserClient userClient;
    @Mock
    private RabbitTemplate rabbitTemplate;
    @Mock
    private CardClient cardClient;

    private NotificationServiceImpl service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new NotificationServiceImpl(userClient, rabbitTemplate, cardClient);
        ReflectionTestUtils.setField(service, "exchange", "exchange");
        ReflectionTestUtils.setField(service, "singleRoutingKey", "single");
        ReflectionTestUtils.setField(service, "bulkRoutingKey", "bulk");
    }

    @Test
    void sendNotification_whenDisabled_skipsPublish() {
        ReflectionTestUtils.setField(service, "rabbitmqEnabled", false);

        service.sendNotification(1, "@john hello", 5);

        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void sendNotification_withAssignedUserAndMentions_publishesSingleAndBulk() {
        ReflectionTestUtils.setField(service, "rabbitmqEnabled", true);
        when(userClient.getUserIdsByUsername(List.of("john", "kate"))).thenReturn(List.of(5, 8, 9));
        when(cardClient.getAssignedUserId(10)).thenReturn(7);

        service.sendNotification(10, "@john hello @kate", 5);

        verify(rabbitTemplate).convertAndSend(eq("exchange"), eq("single"), any(Object.class));
        verify(rabbitTemplate).convertAndSend(eq("exchange"), eq("bulk"), any(Object.class));
    }

    @Test
    void sendNotification_withOnlySelfMentionAndNoAssignee_skipsPublish() {
        ReflectionTestUtils.setField(service, "rabbitmqEnabled", true);
        when(userClient.getUserIdsByUsername(List.of("john"))).thenReturn(List.of(5));
        when(cardClient.getAssignedUserId(10)).thenReturn(null);

        service.sendNotification(10, "@john hello", 5);

        verifyNoInteractions(rabbitTemplate);
    }
}
