package com.flowboard.card_service.service;

import com.flowboard.card_service.dto.CardRequestDto;
import com.flowboard.card_service.dto.CardResponseDto;
import com.flowboard.card_service.dto.CardUpdateDto;
import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;

import java.util.List;

public interface CardService {
    CardResponseDto createCard(CardRequestDto cardRequestDto, Integer userId);

    CardResponseDto getCardById(Integer cardId, Integer userId);

    List<CardResponseDto> getCardsByList(Integer listId, Integer userId);

    List<CardResponseDto> getCardsByBoard(Integer boardId, Integer userId);

    List<CardResponseDto> getCardsByAssignee(Integer assigneeId, Integer userId);

    CardResponseDto updateCard(Integer cardId, CardUpdateDto cardUpdateDto, Integer userId);

    void deleteCard(Integer cardId, Integer userId);

    CardResponseDto moveCard(Integer cardId, Integer targetListId, Integer newPosition, Integer userId);

    void reorderCards(Integer listId, List<Integer> orderedCardIds, Integer userId);

    CardResponseDto assignCard(Integer cardId, Integer assigneeId, Integer userId);

    CardResponseDto updatePriority(Integer cardId, Priority priority, Integer userId);

    CardResponseDto updateStatus(Integer cardId, Status status, Integer userId);

    void archiveCard(Integer cardId, Integer userId);

    void unarchiveCard(Integer cardId, Integer userId);

    List<CardResponseDto> getOverdueCards(Integer userId);

    Integer getAssignedUserId(Integer cardId);
}