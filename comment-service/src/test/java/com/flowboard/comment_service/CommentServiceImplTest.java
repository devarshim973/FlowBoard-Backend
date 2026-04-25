package com.flowboard.comment_service;

import com.flowboard.comment_service.dto.CommentRequestDto;
import com.flowboard.comment_service.dto.CommentResponseDto;
import com.flowboard.comment_service.dto.CommentUpdateDto;
import com.flowboard.comment_service.entity.Comment;
import com.flowboard.comment_service.exception.CommentNotFoundException;
import com.flowboard.comment_service.mapper.impl.CommentRequestMapper;
import com.flowboard.comment_service.mapper.impl.CommentResponseMapper;
import com.flowboard.comment_service.repository.CommentRepository;
import com.flowboard.comment_service.service.NotificationService;
import com.flowboard.comment_service.service.impl.CommentServiceImpl;
import com.flowboard.comment_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CommentServiceImplTest {

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private CommentRequestMapper commentRequestMapper;

    @Mock
    private CommentResponseMapper commentResponseMapper;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private CommentServiceImpl commentService;

    @Test
    void addComment_positive() {

        CommentRequestDto request = new CommentRequestDto();
        request.setCardId(1);
        request.setAuthorId(1);
        request.setContent("hello");

        Comment comment = new Comment();

        CommentResponseDto response = new CommentResponseDto();
        response.setCommentId(1);

        when(commentRequestMapper.mapTo(any(CommentRequestDto.class)))
                .thenReturn(comment);

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        when(commentResponseMapper.mapTo(any(Comment.class)))
                .thenReturn(response);

        CommentResponseDto result =
                commentService.addComment(request);

        assertEquals(1, result.getCommentId());

        verify(notificationService)
                .sendNotification(1, "hello", 1);
    }

    @Test
    void getByCard_positive() {

        Comment comment = new Comment();

        PageImpl<Comment> page =
                new PageImpl<>(
                        List.of(comment),
                        PageRequest.of(0, 5),
                        1
                );

        when(commentRepository.findByCardId(anyInt(), any()))
                .thenReturn(page);

        when(commentResponseMapper.mapTo(any(Comment.class)))
                .thenReturn(new CommentResponseDto());

        CustomPageResponse<CommentResponseDto> result =
                commentService.getByCard(
                        1, 0, 5,
                        "commentId",
                        "asc"
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getCommentById_positive() {

        Comment comment = new Comment();

        CommentResponseDto response = new CommentResponseDto();
        response.setCommentId(1);

        when(commentRepository.findById(1))
                .thenReturn(Optional.of(comment));

        when(commentResponseMapper.mapTo(any(Comment.class)))
                .thenReturn(response);

        CommentResponseDto result =
                commentService.getCommentById(1);

        assertEquals(1, result.getCommentId());
    }

    @Test
    void getCommentById_negative() {

        when(commentRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentService.getCommentById(99));
    }

    @Test
    void getReplies_positive() {

        Comment comment = new Comment();

        PageImpl<Comment> page =
                new PageImpl<>(
                        List.of(comment),
                        PageRequest.of(0, 5),
                        1
                );

        when(commentRepository.findByParentCommentId(anyInt(), any()))
                .thenReturn(page);

        when(commentResponseMapper.mapTo(any(Comment.class)))
                .thenReturn(new CommentResponseDto());

        CustomPageResponse<CommentResponseDto> result =
                commentService.getReplies(
                        1, 0, 5,
                        "commentId",
                        "desc"
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void updateComment_positive() {

        Comment comment = new Comment();

        CommentUpdateDto request =
                new CommentUpdateDto();
        request.setCommentId(1);
        request.setContent("updated");

        CommentResponseDto response =
                new CommentResponseDto();
        response.setCommentId(1);

        when(commentRepository.findById(1))
                .thenReturn(Optional.of(comment));

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment);

        when(commentResponseMapper.mapTo(any(Comment.class)))
                .thenReturn(response);

        CommentResponseDto result =
                commentService.updateComment(request);

        assertEquals(1, result.getCommentId());
    }

    @Test
    void updateComment_negative() {

        CommentUpdateDto request =
                new CommentUpdateDto();
        request.setCommentId(99);

        when(commentRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentService.updateComment(request));
    }

    @Test
    void getCommentCount_positive() {

        when(commentRepository.countByCardId(1))
                .thenReturn(5L);

        Long result =
                commentService.getCommentCount(1);

        assertEquals(5L, result);
    }

    @Test
    void deleteComment_positive() {

        Comment comment = new Comment();

        when(commentRepository.findById(1))
                .thenReturn(Optional.of(comment));

        commentService.deleteComment(1);

        verify(commentRepository).delete(comment);
    }

    @Test
    void deleteComment_negative() {

        when(commentRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(CommentNotFoundException.class,
                () -> commentService.deleteComment(99));
    }
}