package com.flowboard.comment_service.service.impl;

import com.flowboard.comment_service.client.UserClient;
import com.flowboard.comment_service.dto.BulkNotificationRequestDto;
import com.flowboard.comment_service.entity.NotificationType;
import com.flowboard.comment_service.entity.RelatedType;
import com.flowboard.comment_service.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationServiceImpl implements NotificationService {
    private final UserClient userClient;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.exchange.name}")
    private String exchange;

    @Value("${rabbitmq.routing.key}")
    private String routingKey;

    @Override
    public void sendNotification(Integer cardId, String content, Integer currentUserId) {
        // card service is not ready so using a static list of recipients
        Set<Integer> allRecipients = new HashSet<>(List.of(2, 3, 4));

        // remove self
        allRecipients.remove(currentUserId);

        List<String> mentions = extractMentions(content);
        List<Integer> mentionedUserIds = userClient.getUserIdsByUsername(mentions);
        log.info("Returned user ids from user service : " + mentionedUserIds.toString());

        Set<Integer> mentionUsers = new HashSet<>(mentionedUserIds);

        // remove self from mentions
        mentionUsers.remove(currentUserId);

        // split
        Set<Integer> normalUsers = new HashSet<>(allRecipients);
        normalUsers.removeAll(mentionUsers);

        log.info(mentionUsers.toString());
        log.info(normalUsers.toString());
        log.info(currentUserId.toString());

        // send mention notifications
        if (!mentionUsers.isEmpty()) {
            BulkNotificationRequestDto notificationRequestDto = buildDto(new ArrayList<>(mentionUsers),
                    currentUserId,
                    cardId,
                    NotificationType.MENTION,
                    "You were mentioned",
                    "You were mentioned in a comment");
            rabbitTemplate.convertAndSend(exchange, routingKey, notificationRequestDto);

        }

        // send notifications
        if (!normalUsers.isEmpty()) {
            BulkNotificationRequestDto notificationRequestDto = buildDto(new ArrayList<>(normalUsers),
                    currentUserId,
                    cardId,
                    NotificationType.COMMENT,
                    "New Comment",
                    "Someone commented on your card");
            rabbitTemplate.convertAndSend(exchange, routingKey, notificationRequestDto);
        }
    }

    private List<String> extractMentions(String content) {
        String[] words = content.split(" ");
        List<String> result = new ArrayList<>();
        for(String word : words) {
            if(word.charAt(0) == '@') result.add(word.substring(1));
        }
        return result;
    }

    private BulkNotificationRequestDto buildDto(
            List<Integer> recipients,
            Integer actorId,
            Integer cardId,
            NotificationType type,
            String title,
            String message
    ) {
        BulkNotificationRequestDto dto = new BulkNotificationRequestDto();
        dto.setRecipientIds(recipients);
        dto.setActorId(actorId);
        dto.setNotificationType(type);
        dto.setTitle(title);
        dto.setMessage(message);
        dto.setRelatedId(cardId);
        dto.setRelatedType(RelatedType.CARD);
        return dto;
    }
}