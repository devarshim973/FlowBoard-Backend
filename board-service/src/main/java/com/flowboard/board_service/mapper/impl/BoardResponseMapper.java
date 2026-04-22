package com.flowboard.board_service.mapper.impl;

import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardResponseMapper implements Mapper<Board, BoardResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public BoardResponseDto mapTo(Board board) {
        return modelMapper.map(board, BoardResponseDto.class);
    }

    @Override
    public Board mapFrom(BoardResponseDto boardResponseDto) {
        return modelMapper.map(boardResponseDto, Board.class);
    }
}