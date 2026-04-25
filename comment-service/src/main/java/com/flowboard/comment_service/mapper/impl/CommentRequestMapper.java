package com.flowboard.comment_service.mapper.impl;

import com.flowboard.comment_service.dto.CommentRequestDto;
import com.flowboard.comment_service.entity.Comment;
import com.flowboard.comment_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentRequestMapper implements Mapper<CommentRequestDto, Comment> {
    private final ModelMapper mapper;

    @Override
    public Comment mapTo(CommentRequestDto commentRequestDto) {
        return mapper.map(commentRequestDto, Comment.class);
    }

    @Override
    public CommentRequestDto mapFrom(Comment comment) {
        return mapper.map(comment, CommentRequestDto.class);
    }
}
