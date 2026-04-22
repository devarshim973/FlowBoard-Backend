package com.flowboard.card_service.service.impl;

import com.flowboard.card_service.dto.BulkNotificationRequestDto;
import com.flowboard.card_service.dto.NotificationRequestDto;
import com.flowboard.card_service.service.NotificationProcedure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationProducerImpl implements NotificationProcedure {

    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.single.key}")
    private String singleRoutingKey;

    @Value("${rabbitmq.routing.bulk.key}")
    private String bulkRoutingKey;

    @Override
    public void sendBulk(BulkNotificationRequestDto message) {
        log.info("Added a bulk notification in queue");
        rabbitTemplate.convertAndSend(exchange, bulkRoutingKey, message);
    }

    @Override
    public void sendSingle(NotificationRequestDto message) {
        log.info("Added a single notification in queue");
        rabbitTemplate.convertAndSend(exchange, bulkRoutingKey, message);
    }
}