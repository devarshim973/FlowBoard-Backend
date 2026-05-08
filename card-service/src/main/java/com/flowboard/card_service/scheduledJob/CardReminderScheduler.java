package com.flowboard.card_service.scheduledJob;

import com.flowboard.card_service.dto.NotificationRequestDto;
import com.flowboard.card_service.dto.NotificationType;
import com.flowboard.card_service.dto.RelatedType;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.entity.Status;
import com.flowboard.card_service.repository.CardRepository;
import com.flowboard.card_service.service.NotificationProcedure;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CardReminderScheduler {

    private final CardRepository cardRepository;
    private final NotificationProcedure notificationProcedure;

    @Scheduled(cron = "0 * * * * *") // every 1 minute
    public void checkDueCards() {
        log.info("Scheduled job running");
        List<Card> cards = cardRepository.findByIsArchivedFalse();

        LocalDateTime now = LocalDateTime.now();

        for (Card card : cards) {
            if (card.getDueDate() == null) continue;
            if (card.getStatus() == Status.DONE) continue;
            if (card.getAssigneeId() == null) continue;

            long minutesLeft =
                    Duration.between(now, card.getDueDate()).toMinutes();

            log.info(card.toString());

            // due in 1 hr
            if (minutesLeft == 60) {
                log.info("Reminder scheduler found a card pending in next 1 hr id - {}",card.getCardId());
                sendNotification(
                        card,
                        "Upcoming Due Card",
                        "Your card '" + card.getTitle() + "' is due in 1 hour."
                );
            }

            // due in 10 min
            else if (minutesLeft == 10) {
                log.info("Reminder scheduler found a card pending in next 1 hr id - {}",card.getCardId());
                sendNotification(
                        card,
                        "Card Due Soon",
                        "Your card '" + card.getTitle() + "' is due in 10 minutes."
                );
            }
        }

        log.info("Reminder scheduler checked all cards");
    }

    private void sendNotification(Card card,
                                  String title,
                                  String message) {

        NotificationRequestDto dto = NotificationRequestDto
                .builder()
                .actorId(card.getCreatedById())
                .notificationType(NotificationType.DUE_DATE)
                .title(title)
                .message(message)
                .relatedType(RelatedType.CARD)
                .relatedId(card.getCardId())
                .recipientId(card.getAssigneeId())
                .build();

        notificationProcedure.sendSingle(dto);
    }
}