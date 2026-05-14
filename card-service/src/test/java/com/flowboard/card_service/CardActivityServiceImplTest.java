package com.flowboard.card_service;

import com.flowboard.card_service.client.BoardClient;
import com.flowboard.card_service.client.WorkspaceClient;
import com.flowboard.card_service.dto.CardActivityResponseDto;
import com.flowboard.card_service.entity.ActivityType;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.entity.CardActivity;
import com.flowboard.card_service.exception.IllegalOperationException;
import com.flowboard.card_service.mapper.impl.CardActivityResponseMapper;
import com.flowboard.card_service.repository.CardActivityRepository;
import com.flowboard.card_service.repository.CardRepository;
import com.flowboard.card_service.service.impl.CardActivityServiceImpl;
import com.flowboard.card_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CardActivityServiceImplTest {

    @Mock
    private CardActivityRepository cardActivityRepository;
    @Mock
    private CardRepository cardRepository;
    @Mock
    private BoardClient boardClient;
    @Mock
    private WorkspaceClient workspaceClient;
    @Mock
    private CardActivityResponseMapper responseMapper;

    @InjectMocks
    private CardActivityServiceImpl service;

    @Test
    void logActivity_savesAndMaps() {
        CardActivity activity = new CardActivity();
        CardActivityResponseDto dto = new CardActivityResponseDto();

        when(cardActivityRepository.save(any(CardActivity.class))).thenReturn(activity);
        when(responseMapper.mapTo(activity)).thenReturn(dto);

        CardActivityResponseDto result = service.logActivity(1, 2, ActivityType.CREATED, "created");

        assertEquals(dto, result);
        verify(cardActivityRepository).save(any(CardActivity.class));
    }

    @Test
    void getActivitiesByCard_forPrivateBoardMember_returnsPage() {
        Card card = new Card();
        card.setBoardId(10);
        CardActivity activity = new CardActivity();
        CardActivityResponseDto dto = new CardActivityResponseDto();

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(boardClient.isPrivate(10)).thenReturn(true);
        when(boardClient.isMember(10, 5)).thenReturn(true);
        when(cardActivityRepository.findByCardIdOrderByCreatedAtDesc(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(responseMapper.mapTo(activity)).thenReturn(dto);

        CustomPageResponse<CardActivityResponseDto> result =
                service.getActivitiesByCard(1, 0, 10, "createdAt", "desc", 5);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getActivitiesByCard_forPrivateBoardNonMember_throws() {
        Card card = new Card();
        card.setBoardId(10);

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(boardClient.isPrivate(10)).thenReturn(true);
        when(boardClient.isMember(10, 5)).thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> service.getActivitiesByCard(1, 0, 10, "createdAt", "desc", 5));
    }

    @Test
    void getActivitiesByCard_forPrivateWorkspaceMember_returnsPage() {
        Card card = new Card();
        card.setBoardId(10);
        CardActivity activity = new CardActivity();
        CardActivityResponseDto dto = new CardActivityResponseDto();

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(boardClient.isPrivate(10)).thenReturn(false);
        when(boardClient.getWorkspaceId(10)).thenReturn(20);
        when(workspaceClient.isPrivate(20)).thenReturn(true);
        when(workspaceClient.isMember(20, 5)).thenReturn(true);
        when(cardActivityRepository.findByCardIdOrderByCreatedAtDesc(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(responseMapper.mapTo(activity)).thenReturn(dto);

        CustomPageResponse<CardActivityResponseDto> result =
                service.getActivitiesByCard(1, 0, 10, "createdAt", "asc", 5);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getActivitiesByCard_forPrivateWorkspaceNonMember_throws() {
        Card card = new Card();
        card.setBoardId(10);

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(boardClient.isPrivate(10)).thenReturn(false);
        when(boardClient.getWorkspaceId(10)).thenReturn(20);
        when(workspaceClient.isPrivate(20)).thenReturn(true);
        when(workspaceClient.isMember(20, 5)).thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> service.getActivitiesByCard(1, 0, 10, "createdAt", "asc", 5));
    }

    @Test
    void getActivitiesByCard_forPublicWorkspace_returnsPage() {
        Card card = new Card();
        card.setBoardId(10);
        CardActivity activity = new CardActivity();
        CardActivityResponseDto dto = new CardActivityResponseDto();

        when(cardRepository.findById(1)).thenReturn(Optional.of(card));
        when(boardClient.isPrivate(10)).thenReturn(false);
        when(boardClient.getWorkspaceId(10)).thenReturn(20);
        when(workspaceClient.isPrivate(20)).thenReturn(false);
        when(cardActivityRepository.findByCardIdOrderByCreatedAtDesc(eq(1), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(activity)));
        when(responseMapper.mapTo(activity)).thenReturn(dto);

        CustomPageResponse<CardActivityResponseDto> result =
                service.getActivitiesByCard(1, 0, 10, "createdAt", "asc", 5);

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getActivitiesByCard_whenCardMissing_throws() {
        when(cardRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(IllegalOperationException.class,
                () -> service.getActivitiesByCard(99, 0, 10, "createdAt", "asc", 5));
    }
}
