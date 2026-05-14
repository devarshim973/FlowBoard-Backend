package com.flowboard.card_service;

import com.flowboard.card_service.config.AppConfig;
import com.flowboard.card_service.config.RabbitMQConfig;
import com.flowboard.card_service.dto.BulkNotificationRequestDto;
import com.flowboard.card_service.dto.NotificationRequestDto;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.entity.Status;
import com.flowboard.card_service.exception.ServiceUnavailableException;
import com.flowboard.card_service.fallback.BoardFallback;
import com.flowboard.card_service.fallback.ListFallback;
import com.flowboard.card_service.fallback.NotificationFallback;
import com.flowboard.card_service.fallback.WorkspaceFallback;
import com.flowboard.card_service.repository.CardRepository;
import com.flowboard.card_service.scheduledJob.CardReminderScheduler;
import com.flowboard.card_service.service.NotificationProcedure;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.boot.SpringApplication;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CardInfrastructureTest {

    @Test
    void mainShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            CardServiceApplication.main(new String[]{"--test"});

            springApplication.verify(() -> SpringApplication.run(CardServiceApplication.class, new String[]{"--test"}));
        }
    }

    @Test
    void appConfigShouldUseStrictMatchingStrategy() {
        ModelMapper modelMapper = new AppConfig().modelMapper();

        assertEquals(MatchingStrategies.STRICT, modelMapper.getConfiguration().getMatchingStrategy());
    }

    @Test
    void rabbitMqConfigShouldCreateQueuesExchangeBindingsAndConverter() {
        RabbitMQConfig config = new RabbitMQConfig();
        ReflectionTestUtils.setField(config, "singleQueue", "single.queue");
        ReflectionTestUtils.setField(config, "bulkQueue", "bulk.queue");
        ReflectionTestUtils.setField(config, "exchange", "flowboard.exchange");
        ReflectionTestUtils.setField(config, "singleRoutingKey", "single.key");
        ReflectionTestUtils.setField(config, "bulkRoutingKey", "bulk.key");

        Queue singleQueue = config.singleNotificationQueue();
        Queue bulkQueue = config.bulkNotificationQueue();
        DirectExchange exchange = config.notificationExchange();

        assertEquals("single.queue", singleQueue.getName());
        assertEquals("bulk.queue", bulkQueue.getName());
        assertEquals("flowboard.exchange", exchange.getName());
        assertEquals("single.key", config.singleNotificationBinding(singleQueue, exchange).getRoutingKey());
        assertEquals("bulk.key", config.bulkNotificationBinding(bulkQueue, exchange).getRoutingKey());
        assertInstanceOf(org.springframework.amqp.support.converter.Jackson2JsonMessageConverter.class, config.jsonMessageConverter());
    }

    @Test
    void fallbackClientsShouldThrowServiceUnavailableException() {
        BoardFallback boardFallback = new BoardFallback();
        WorkspaceFallback workspaceFallback = new WorkspaceFallback();
        ListFallback listFallback = new ListFallback();
        NotificationFallback notificationFallback = new NotificationFallback();

        assertThrows(ServiceUnavailableException.class, () -> boardFallback.isMember(1, 1));
        assertThrows(ServiceUnavailableException.class, () -> boardFallback.getWorkspaceId(1));
        assertThrows(ServiceUnavailableException.class, () -> boardFallback.isPrivate(1));
        assertThrows(ServiceUnavailableException.class, () -> workspaceFallback.isMember(1, 1));
        assertThrows(ServiceUnavailableException.class, () -> workspaceFallback.isPrivate(1));
        assertThrows(ServiceUnavailableException.class, () -> listFallback.getBoardId(1));
        assertThrows(ServiceUnavailableException.class, () -> notificationFallback.handleSendNotification(new NotificationRequestDto()));
        assertThrows(ServiceUnavailableException.class, () -> notificationFallback.handleSendBulkNotification(new BulkNotificationRequestDto()));
    }

    @Test
    void schedulerShouldNotifyOnlyCardsNearDeadline() {
        CardRepository cardRepository = mock(CardRepository.class);
        NotificationProcedure notificationProcedure = mock(NotificationProcedure.class);
        CardReminderScheduler scheduler = new CardReminderScheduler(cardRepository, notificationProcedure);

        LocalDateTime now = LocalDateTime.now();
        Card dueInHour = buildCard(1, "Design", now.plusMinutes(60).plusSeconds(5), Status.TO_DO, 10);
        Card dueInTenMinutes = buildCard(2, "Review", now.plusMinutes(10).plusSeconds(5), Status.IN_PROGRESS, 11);
        Card doneCard = buildCard(3, "Done", now.plusMinutes(60), Status.DONE, 12);
        Card withoutAssignee = buildCard(4, "Unassigned", now.plusMinutes(10).plusSeconds(5), Status.TO_DO, null);
        Card withoutDueDate = buildCard(5, "Backlog", null, Status.TO_DO, 13);

        when(cardRepository.findByIsArchivedFalse()).thenReturn(List.of(
                dueInHour,
                dueInTenMinutes,
                doneCard,
                withoutAssignee,
                withoutDueDate
        ));

        scheduler.checkDueCards();

        ArgumentCaptor<NotificationRequestDto> captor = ArgumentCaptor.forClass(NotificationRequestDto.class);
        verify(notificationProcedure, org.mockito.Mockito.times(2)).sendSingle(captor.capture());
        List<NotificationRequestDto> notifications = captor.getAllValues();
        assertTrue(notifications.stream().anyMatch(n -> n.getRelatedId().equals(1) && n.getTitle().equals("Upcoming Due Card")));
        assertTrue(notifications.stream().anyMatch(n -> n.getRelatedId().equals(2) && n.getTitle().equals("Card Due Soon")));
    }

    private Card buildCard(Integer id, String title, LocalDateTime dueDate, Status status, Integer assigneeId) {
        Card card = new Card();
        card.setCardId(id);
        card.setTitle(title);
        card.setDueDate(dueDate);
        card.setStatus(status);
        card.setAssigneeId(assigneeId);
        card.setCreatedById(99);
        return card;
    }
}
