package com.flowboard.card_service;

import com.flowboard.card_service.client.BoardClient;
import com.flowboard.card_service.client.ListClient;
import com.flowboard.card_service.client.WorkspaceClient;
import com.flowboard.card_service.dto.CardRequestDto;
import com.flowboard.card_service.dto.CardResponseDto;
import com.flowboard.card_service.dto.CardUpdateDto;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import com.flowboard.card_service.exception.CardNotFoundException;
import com.flowboard.card_service.exception.IllegalOperationException;
import com.flowboard.card_service.mapper.impl.CardRequestMapper;
import com.flowboard.card_service.mapper.impl.CardResponseMapper;
import com.flowboard.card_service.repository.CardRepository;
import com.flowboard.card_service.service.NotificationProcedure;
import com.flowboard.card_service.service.impl.CardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hibernate.validator.internal.util.Contracts.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardServiceImplTest {

    @Mock
    private CardRepository cardRepository;

    @Mock
    private CardRequestMapper requestMapper;

    @Mock
    private CardResponseMapper responseMapper;

    @Mock
    private WorkspaceClient workspaceClient;

    @Mock
    private BoardClient boardClient;

    @Mock
    private ListClient listClient;

    @Mock
    private NotificationProcedure notificationProducer;

    @InjectMocks
    private CardServiceImpl cardService;

    private Card getCard() {
        Card card = new Card();
        card.setCardId(1);
        card.setBoardId(1);
        card.setListId(1);
        card.setPosition(1);
        card.setTitle("Task");
        card.setPriority(Priority.HIGH);
        card.setStatus(Status.TO_DO);
        card.setAssigneeId(2);
        card.setCreatedById(1);
        card.setIsArchived(false);
        return card;
    }

    private void allowModification() {
        when(boardClient.isPrivate(1)).thenReturn(false);
        when(boardClient.getWorkspaceId(1)).thenReturn(10);
        when(workspaceClient.isMember(10, 1)).thenReturn(true);
    }

    private void allowView() {
        when(boardClient.isPrivate(1)).thenReturn(false);
        when(boardClient.getWorkspaceId(1)).thenReturn(10);
    }

    @Test
    void createCard_positive() {

        CardRequestDto dto = new CardRequestDto();

        Card card = new Card();
        card.setBoardId(10);
        card.setListId(20);
        card.setAssigneeId(5);
        card.setPriority(Priority.HIGH);
        card.setTitle("Task");

        Card saved = new Card();
        saved.setCardId(1);
        saved.setBoardId(10);

        CardResponseDto response = new CardResponseDto();

        when(requestMapper.mapTo(dto)).thenReturn(card);

        when(boardClient.isPrivate(10)).thenReturn(false);
        when(boardClient.getWorkspaceId(10)).thenReturn(100);

        when(workspaceClient.isMember(100, 1)).thenReturn(true); // creator
        when(workspaceClient.isMember(100, 5)).thenReturn(true); // assignee

        when(cardRepository.maxPosition(20)).thenReturn(0);
        when(cardRepository.save(card)).thenReturn(saved);

        when(responseMapper.mapTo(saved)).thenReturn(response);

        CardResponseDto result = cardService.createCard(dto, 1);

        assertNotNull(result);
    }

    @Test
    void createCard_negative() {

        CardRequestDto dto = new CardRequestDto();
        Card card = getCard();

        when(requestMapper.mapTo(dto)).thenReturn(card);
        when(boardClient.isPrivate(1)).thenReturn(false);
        when(boardClient.getWorkspaceId(1)).thenReturn(10);
        when(workspaceClient.isMember(10, 1)).thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> cardService.createCard(dto, 1));
    }

    @Test
    void getCardById_positive() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        allowView();

        when(responseMapper.mapTo(any(Card.class)))
                .thenReturn(new CardResponseDto());

        cardService.getCardById(1, 1);
    }

    @Test
    void getCardById_negative() {

        when(cardRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(CardNotFoundException.class,
                () -> cardService.getCardById(99, 1));
    }

    @Test
    void getCardsByList_positive() {

        Card card = getCard();

        when(cardRepository.findByListIdAndIsArchivedFalseOrderByPosition(1))
                .thenReturn(List.of(card));

        allowView();

        when(responseMapper.mapTo(any(Card.class)))
                .thenReturn(new CardResponseDto());

        assertEquals(1,
                cardService.getCardsByList(1, 1).size());
    }

    @Test
    void getCardsByBoard_positive() {

        Card card = getCard();

        when(cardRepository.findByBoardIdOrderByPosition(1))
                .thenReturn(List.of(card));

        allowView();

        when(responseMapper.mapTo(any(Card.class)))
                .thenReturn(new CardResponseDto());

        assertEquals(1,
                cardService.getCardsByBoard(1, 1).size());
    }

    @Test
    void updateCard_positive() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        allowModification();

        when(cardRepository.save(card))
                .thenReturn(card);

        when(responseMapper.mapTo(any(Card.class)))
                .thenReturn(new CardResponseDto());

        cardService.updateCard(1,
                new CardUpdateDto(),
                1);
    }

    @Test
    void deleteCard_positive() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        allowModification();

        cardService.deleteCard(1, 1);

        verify(cardRepository).delete(card);
    }

    @Test
    void moveCard_positive() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        allowModification();

        when(listClient.getBoardId(2))
                .thenReturn(1);

        when(cardRepository.findByListIdAndIsArchivedFalseOrderByPosition(1))
                .thenReturn(new ArrayList<>(List.of(card)));

        when(cardRepository.findByListIdAndIsArchivedFalseOrderByPosition(2))
                .thenReturn(new ArrayList<>());

        when(responseMapper.mapTo(any(Card.class)))
                .thenReturn(new CardResponseDto());

        cardService.moveCard(1, 2, 1, 1);
    }

    @Test
    void moveCard_negative() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        allowModification();

        when(listClient.getBoardId(2))
                .thenReturn(99);

        assertThrows(IllegalOperationException.class,
                () -> cardService.moveCard(1, 2, 1, 1));
    }

    @Test
    void reorderCards_positive() {

        Card c1 = getCard();
        Card c2 = getCard();
        c2.setCardId(2);

        when(cardRepository.findByListIdAndIsArchivedFalseOrderByPosition(1))
                .thenReturn(List.of(c1, c2));

        allowModification();

        cardService.reorderCards(
                1,
                List.of(2, 1),
                1
        );

        verify(cardRepository).saveAll(anyList());
    }

    @Test
    void assignCard_positive() {

        Card card = new Card();
        card.setCardId(1);
        card.setBoardId(10);
        card.setTitle("Task");

        CardResponseDto response = new CardResponseDto();

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));

        when(boardClient.isPrivate(10)).thenReturn(false);
        when(boardClient.getWorkspaceId(10)).thenReturn(100);

        when(workspaceClient.isMember(100, 1)).thenReturn(true);

        // second validation uses assigneeId as boardId in your code
        when(boardClient.isPrivate(5)).thenReturn(false);
        when(boardClient.getWorkspaceId(5)).thenReturn(100);
        when(workspaceClient.isMember(100, 1)).thenReturn(true);

        when(cardRepository.save(card)).thenReturn(card);
        when(responseMapper.mapTo(card)).thenReturn(response);

        CardResponseDto result = cardService.assignCard(1, 5, 1);

        assertNotNull(result);
    }

    @Test
    void updatePriority_positive() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        allowModification();

        when(cardRepository.save(card))
                .thenReturn(card);

        when(responseMapper.mapTo(any(Card.class)))
                .thenReturn(new CardResponseDto());

        cardService.updatePriority(
                1,
                Priority.CRITICAL,
                1
        );
    }

    @Test
    void updateStatus_positive() {

        Card card = new Card();
        card.setCardId(1);
        card.setBoardId(10);

        CardResponseDto response = new CardResponseDto();

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));

        when(boardClient.isPrivate(10)).thenReturn(false);
        when(boardClient.getWorkspaceId(10)).thenReturn(100);
        when(workspaceClient.isMember(100, 1)).thenReturn(true);

        when(cardRepository.save(card)).thenReturn(card);
        when(responseMapper.mapTo(card)).thenReturn(response);

        CardResponseDto result =
                cardService.updateStatus(1, Status.DONE, 1);

        assertNotNull(result);
    }

    @Test
    void archiveCard_positive() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        allowModification();

        cardService.archiveCard(1, 1);

        verify(cardRepository).save(card);
    }

    @Test
    void unarchiveCard_positive() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        allowModification();

        cardService.unarchiveCard(1, 1);

        verify(cardRepository).save(card);
    }

    @Test
    void getOverdueCards_positive() {

        Card card = getCard();

        when(cardRepository
                .findByAssigneeIdAndDueDateBeforeAndStatusNotOrderByPositionAsc(
                        eq(1),
                        any(LocalDateTime.class),
                        eq(Status.DONE)
                ))
                .thenReturn(List.of(card));

        when(responseMapper.mapTo(any(Card.class)))
                .thenReturn(new CardResponseDto());

        assertEquals(1,
                cardService.getOverdueCards(1).size());
    }

    @Test
    void getAssignedUserId_positive() {

        Card card = getCard();

        when(cardRepository.findById(1))
                .thenReturn(Optional.of(card));

        assertEquals(2,
                cardService.getAssignedUserId(1));
    }
}