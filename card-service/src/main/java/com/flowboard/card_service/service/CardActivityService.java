package com.flowboard.card_service.service;

import com.flowboard.card_service.dto.CardActivityResponseDto;
import com.flowboard.card_service.entity.ActivityType;
import com.flowboard.card_service.util.CustomPageResponse;

import java.time.LocalDateTime;

public interface CardActivityService {

    CardActivityResponseDto logActivity(Integer cardId,
                                        Integer actorId,
                                        ActivityType activityType,
                                        String details);

    CustomPageResponse<CardActivityResponseDto> getActivitiesByCard(
            Integer cardId,
            int page,
            int size,
            String sortBy,
            String direction,
            Integer userId
    );
}