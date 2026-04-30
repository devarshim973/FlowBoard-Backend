package com.flowboard.card_service.mapper.impl;

import com.flowboard.card_service.dto.CardRequestDto;
import com.flowboard.card_service.entity.Card;
import com.flowboard.card_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

@Component
@RequiredArgsConstructor
public class CardRequestMapper implements Mapper<CardRequestDto, Card> {
    private final ModelMapper modelMapper;
    @Override
    public Card mapTo(CardRequestDto cardRequestDto) {
        return modelMapper.map(cardRequestDto, Card.class);
    }

    @Override
    public CardRequestDto mapFrom(Card card) {
        return modelMapper.map(card, CardRequestDto.class);
    }
}
