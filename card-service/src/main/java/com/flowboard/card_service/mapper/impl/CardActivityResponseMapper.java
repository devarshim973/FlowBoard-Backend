package com.flowboard.card_service.mapper.impl;

import com.flowboard.card_service.dto.CardActivityResponseDto;
import com.flowboard.card_service.entity.CardActivity;
import com.flowboard.card_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CardActivityResponseMapper implements Mapper<CardActivity, CardActivityResponseDto> {
    private final ModelMapper modelMapper;

    @Override
    public CardActivityResponseDto mapTo(CardActivity cardActivity) {
        return modelMapper.map(cardActivity, CardActivityResponseDto.class);
    }

    @Override
    public CardActivity mapFrom(CardActivityResponseDto cardActivityResponseDto) {
        return modelMapper.map(cardActivityResponseDto, CardActivity.class);
    }
}
