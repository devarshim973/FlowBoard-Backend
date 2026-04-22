package com.flowboard.comment_service.mapper.impl;

import com.flowboard.comment_service.dto.CommentResponseDto;
import com.flowboard.comment_service.entity.Comment;
import com.flowboard.comment_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentResponseMapper implements Mapper<Comment, CommentResponseDto> {
    private final ModelMapper mapper;
    @Override
    public CommentResponseDto mapTo(Comment comment) {
        return mapper.map(comment, CommentResponseDto.class);
    }

    @Override
    public Comment mapFrom(CommentResponseDto commentResponseDto) {
        return mapper.map(commentResponseDto, Comment.class);
    }
}
