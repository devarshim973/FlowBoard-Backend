package com.flowboard.card_service.mapper.impl;

import com.flowboard.card_service.dto.CardResponseDto;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CardResponseMapper implements Mapper<Card, CardResponseDto> {
    private final ModelMapper modelMapper;

    @Override
    public CardResponseDto mapTo(Card card) {
        return modelMapper.map(card, CardResponseDto.class);
    }

    @Override
    public Card mapFrom(CardResponseDto cardResponseDto) {
        return modelMapper.map(cardResponseDto, Card.class);
    }
}
