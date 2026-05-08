package com.flowboard.board_service.mapper.impl;

import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.entity.BoardMember;
import com.flowboard.board_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardMemberRequestMapper implements Mapper<BoardMemberRequestDto, BoardMember> {

    private final ModelMapper modelMapper;

    @Override
    public BoardMember mapTo(BoardMemberRequestDto boardMemberRequestDto) {
        return modelMapper.map(boardMemberRequestDto, BoardMember.class);
    }

    @Override
    public BoardMemberRequestDto mapFrom(BoardMember boardMember) {
        return modelMapper.map(boardMember, BoardMemberRequestDto.class);
    }
}