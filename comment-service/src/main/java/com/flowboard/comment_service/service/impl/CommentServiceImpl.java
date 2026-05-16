package com.flowboard.comment_service.service.impl;

import com.flowboard.comment_service.dto.CommentRequestDto;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.dto.CommentResponseDto;
import com.flowboard.comment_service.dto.CommentUpdateDto;
import com.flowboard.comment_service.entity.Comment;
import com.flowboard.comment_service.exception.CommentNotFoundException;
import com.flowboard.comment_service.mapper.impl.CommentRequestMapper;
import com.flowboard.comment_service.mapper.impl.CommentResponseMapper;
import com.flowboard.comment_service.repository.AttachmentRepository;
import com.flowboard.comment_service.repository.CommentRepository;
import com.flowboard.comment_service.service.CommentService;
import com.flowboard.comment_service.service.NotificationService;
import com.flowboard.comment_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;


/*
here we first need to get card by calling card service with the help of cardId and then
1. we need to get all the user who created the card and all the assigne to the card

2. get all the assigne and mentioned user

3. remove the user who added the comment and also remove the user from normal list
who are present in mentioned list
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommentServiceImpl implements CommentService {
    private final AttachmentRepository attachmentRepository;
    private final CommentRepository commentRepository;
    private final CommentRequestMapper commentRequestMapper;
    private final CommentResponseMapper commentResponseMapper;
    private final NotificationService notificationService;

    @Override
    public CommentResponseDto addComment(CommentRequestDto commentRequestDto) {
        log.info("Add comment requested for card {} by user {}", commentRequestDto.getCardId(), commentRequestDto.getAuthorId());
        Comment comment = commentRequestMapper.mapTo(commentRequestDto);
        Comment savedComment = commentRepository.save(comment);
        notificationService.sendNotification(commentRequestDto.getCardId(), commentRequestDto.getContent(), commentRequestDto.getAuthorId());
        log.info("Comment created with id {}", savedComment.getCommentId());
        return mapCommentResponse(savedComment);
    }

    @Override
    public CustomPageResponse<CommentResponseDto> getByCard(Integer cardId,
                                                           int page,
                                                           int size,
                                                           String sortBy,
                                                           String direction) {
        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(sortBy).ascending();
        else sort = Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> commentPage = commentRepository.findByCardId(cardId, pageable);

        Page<CommentResponseDto> commentResponseDtoPage = commentPage
                .map(this::mapCommentResponse);

        CustomPageResponse<CommentResponseDto> response =
                new CustomPageResponse<>(commentResponseDtoPage);

        return response;
    }

    @Override
    public CommentResponseDto getCommentById(Integer commentId) {
        return mapCommentResponse(getComment(commentId));
    }

    @Override
    public CustomPageResponse<CommentResponseDto> getReplies(Integer commentId,
                                                             int page,
                                                             int size,
                                                             String sortBy,
                                                             String direction) {
        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(sortBy).ascending();
        else sort = Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Comment> commentPage = commentRepository
                .findByParentCommentId(commentId, pageable);

        Page<CommentResponseDto> commentResponseDtoPage = commentPage
                .map(this::mapCommentResponse);

        CustomPageResponse<CommentResponseDto> response =
                new CustomPageResponse<>(commentResponseDtoPage);

        return response;
    }

    @Override
    public CommentResponseDto updateComment(CommentUpdateDto commentUpdateDto) {
        Integer commentId = commentUpdateDto.getCommentId();
        log.info("Update comment requested for comment {}", commentId);
        Comment comment = getComment(commentId);

        comment.setContent(commentUpdateDto.getContent());

        Comment savedComment = commentRepository.save(comment);
        log.info("Comment updated with id {}", savedComment.getCommentId());

        return mapCommentResponse(savedComment);
    }

    @Override
    public Long getCommentCount(Integer cardId) {
        return commentRepository.countByCardId(cardId);
    }

    @Override
    public void deleteComment(Integer commentId) {
        log.info("Delete comment requested for comment {}", commentId);
        Comment comment = getComment(commentId);
        attachmentRepository.deleteByCommentId(commentId);
        commentRepository.delete(comment);
        log.info("Comment deleted with id {}", commentId);
    }

    private Comment getComment(Integer commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException("Comment not found with id " + commentId));
    }

    private CommentResponseDto mapCommentResponse(Comment comment) {
        CommentResponseDto response = commentResponseMapper.mapTo(comment);
        response.setAttachments(
                attachmentRepository.findByCommentId(comment.getCommentId()).stream()
                        .map(attachment -> AttachmentResponseDto.builder()
                                .attachmentId(attachment.getAttachmentId())
                                .cardId(attachment.getCardId())
                                .commentId(attachment.getCommentId())
                                .uploaderId(attachment.getUploaderId())
                                .fileName(attachment.getFileName())
                                .fileUrl(attachment.getFileUrl())
                                .fileType(attachment.getFileType())
                                .sizeKb(attachment.getSizeKb())
                                .uploadedAt(attachment.getUploadedAt())
                                .build())
                        .toList()
        );
        return response;
    }
}
