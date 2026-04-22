package com.flowboard.card_service.service.impl;

import com.flowboard.card_service.client.BoardClient;
import com.flowboard.card_service.client.WorkspaceClient;
import com.flowboard.card_service.dto.CardActivityResponseDto;
import com.flowboard.card_service.entity.ActivityType;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.entity.CardActivity;
import com.flowboard.card_service.exception.IllegalOperationException;
import com.flowboard.card_service.mapper.Mapper;
import com.flowboard.card_service.repository.CardActivityRepository;
import com.flowboard.card_service.repository.CardRepository;
import com.flowboard.card_service.service.CardActivityService;
import com.flowboard.card_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CardActivityServiceImpl implements CardActivityService {

    private final CardActivityRepository cardActivityRepository;
    private final CardRepository cardRepository;
    private final BoardClient boardClient;
    private final WorkspaceClient workspaceClient;
    private final Mapper<CardActivity, CardActivityResponseDto> responseMapper;

    private Pageable getPageable(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc")
                ? Sort.by(sortBy).descending()
                : Sort.by(sortBy).ascending();

        return PageRequest.of(page, size, sort);
    }

    @Override
    public CardActivityResponseDto logActivity(Integer cardId,
                                               Integer actorId,
                                               ActivityType activityType,
                                               String details) {

        CardActivity activity = new CardActivity();
        activity.setCardId(cardId);
        activity.setActorId(actorId);
        activity.setActivityType(activityType);
        activity.setDetails(details);

        CardActivity saved = cardActivityRepository.save(activity);

        return responseMapper.mapTo(saved);
    }

    @Override
    public CustomPageResponse<CardActivityResponseDto> getActivitiesByCard(
            Integer cardId,
            int page,
            int size,
            String sortBy,
            String direction,
            Integer userId) {

        Integer boardId = getBoardIdFromCard(cardId);

        validateViewRequest(boardId, userId);

        Pageable pageable = getPageable(page, size, sortBy, direction);

        Page<CardActivity> activityPage = cardActivityRepository
                .findByCardIdOrderByCreatedAtDesc(cardId, pageable);

        Page<CardActivityResponseDto> dtoPage =
                activityPage.map(responseMapper::mapTo);

        return new CustomPageResponse<>(dtoPage);
    }

    private Integer getBoardIdFromCard(Integer cardId) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new IllegalOperationException("Card not found"));

        return card.getBoardId();
    }

    private void validateViewRequest(Integer boardId, Integer userId) {
        if (boardClient.isPrivate(boardId)) {
            if (boardClient.isMember(boardId, userId)) return;
            throw new IllegalOperationException("Not allowed to view activity");
        }

        Integer workspaceId = boardClient.getWorkspaceId(boardId);

        if (workspaceClient.isPrivate(workspaceId)) {
            if (workspaceClient.isMember(workspaceId, userId)) return;
            throw new IllegalOperationException("Not allowed to view activity");
        }
    }
}