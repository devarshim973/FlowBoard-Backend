package com.flowboard.comment_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.comment_service.controller.CommentServiceController;
import com.flowboard.comment_service.dto.CommentRequestDto;
import com.flowboard.comment_service.dto.CommentResponseDto;
import com.flowboard.comment_service.dto.CommentUpdateDto;
import com.flowboard.comment_service.service.CommentService;
import com.flowboard.comment_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
class CommentServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CommentService commentService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomPageResponse<CommentResponseDto> getPage() {

        PageImpl<CommentResponseDto> page =
                new PageImpl<>(
                        List.of(new CommentResponseDto()),
                        PageRequest.of(0, 5),
                        1
                );

        return new CustomPageResponse<>(page);
    }

    @Test
    void handelCreateComment_positive() throws Exception {

        CommentRequestDto request =
                new CommentRequestDto();

        request.setCardId(1);
        request.setAuthorId(1);
        request.setContent("hello");

        CommentResponseDto response =
                new CommentResponseDto();

        response.setCommentId(1);

        when(commentService.addComment(any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.commentId").value(1));
    }

    @Test
    void handelCreateComment_negative() throws Exception {

        mockMvc.perform(post("/api/v1/comments/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handelGetAllByCard_positive() throws Exception {

        when(commentService.getByCard(
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString()))
                .thenReturn(getPage());

        mockMvc.perform(get("/api/v1/comments/card/1"))
                .andExpect(status().isOk());
    }

    @Test
    void handelGetAllByCard_negative() throws Exception {

        when(commentService.getByCard(
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/comments/card/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handelGetCommentById_positive() throws Exception {

        CommentResponseDto response =
                new CommentResponseDto();

        response.setCommentId(1);

        when(commentService.getCommentById(1))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/comments/get/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.commentId").value(1));
    }

    @Test
    void handelGetCommentById_negative() throws Exception {

        when(commentService.getCommentById(99))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/comments/get/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handelGetCommentReplies_positive() throws Exception {

        when(commentService.getReplies(
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString()))
                .thenReturn(getPage());

        mockMvc.perform(get("/api/v1/comments/replies/1"))
                .andExpect(status().isOk());
    }

    @Test
    void handelGetCommentReplies_negative() throws Exception {

        when(commentService.getReplies(
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/comments/replies/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handelUpdateComment_positive() throws Exception {

        CommentUpdateDto request =
                new CommentUpdateDto();

        request.setCommentId(1);
        request.setContent("updated");

        CommentResponseDto response =
                new CommentResponseDto();

        response.setCommentId(1);

        when(commentService.updateComment(any()))
                .thenReturn(response);

        mockMvc.perform(patch("/api/v1/comments/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.commentId").value(1));
    }

    @Test
    void handelUpdateComment_negative() throws Exception {

        mockMvc.perform(patch("/api/v1/comments/update")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handelGetCommentCountForCard_positive() throws Exception {

        when(commentService.getCommentCount(1))
                .thenReturn(5L);

        mockMvc.perform(get("/api/v1/comments/count/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("5"));
    }

    @Test
    void handelGetCommentCountForCard_negative() throws Exception {

        when(commentService.getCommentCount(99))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/comments/count/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void handelDeleteComment_positive() throws Exception {

        doNothing().when(commentService)
                .deleteComment(1);

        mockMvc.perform(delete("/api/v1/comments/delete/1"))
                .andExpect(status().isAccepted())
                .andExpect(content()
                        .string("Comment deleted successfully"));
    }

    @Test
    void handelDeleteComment_negative() throws Exception {

        doThrow(new RuntimeException())
                .when(commentService)
                .deleteComment(99);

        mockMvc.perform(delete("/api/v1/comments/delete/99"))
                .andExpect(status().isBadRequest());
    }
}