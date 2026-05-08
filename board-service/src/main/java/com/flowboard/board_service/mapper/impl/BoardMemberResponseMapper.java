package com.flowboard.board_service.mapper.impl;

import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.entity.BoardMember;
import com.flowboard.board_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardMemberResponseMapper implements Mapper<BoardMember, BoardMemberResponseDto> {

    private final ModelMapper modelMapper;

    @Override
    public BoardMemberResponseDto mapTo(BoardMember boardMember) {
        return modelMapper.map(boardMember, BoardMemberResponseDto.class);
    }

    @Override
    public BoardMember mapFrom(BoardMemberResponseDto boardMemberResponseDto) {
        return modelMapper.map(boardMemberResponseDto, BoardMember.class);
    }
}