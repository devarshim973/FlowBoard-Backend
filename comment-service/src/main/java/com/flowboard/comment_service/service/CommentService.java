package com.flowboard.comment_service.service;

import com.flowboard.comment_service.dto.CommentRequestDto;
import com.flowboard.comment_service.dto.CommentResponseDto;
import com.flowboard.comment_service.dto.CommentUpdateDto;
import com.flowboard.comment_service.util.CustomPageResponse;

import java.util.List;

public interface CommentService {
    public CommentResponseDto addComment(CommentRequestDto commentRequestDto);

    public CustomPageResponse<CommentResponseDto> getByCard(Integer cardId,
                                                           int page,
                                                           int size,
                                                           String sortBy,
                                                           String direction);

    public CommentResponseDto getCommentById(Integer commentId);

    public CustomPageResponse<CommentResponseDto> getReplies(Integer commentId,
                                                             int page,
                                                             int size,
                                                             String sortBy,
                                                             String direction);

    public CommentResponseDto updateComment(CommentUpdateDto commentUpdateDto);

    public Long getCommentCount(Integer cardId);

    public void deleteComment(Integer commentId);
}
