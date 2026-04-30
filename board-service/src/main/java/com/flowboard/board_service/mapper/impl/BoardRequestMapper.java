package com.flowboard.board_service.mapper.impl;

import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardRequestMapper implements Mapper<BoardRequestDto, Board> {
    private final ModelMapper modelMapper;

    @Override
    public Board mapTo(BoardRequestDto boardRequestDto) {
        return modelMapper.map(boardRequestDto, Board.class);
    }

    @Override
    public BoardRequestDto mapFrom(Board board) {
        return modelMapper.map(board, BoardRequestDto.class);
    }
}