package com.flowboard.board_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.board_service.controller.BoardController;
import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.dto.BoardUpdateRequestDto;
import com.flowboard.board_service.service.BoardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardService boardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createBoard_withValidData_returns200() throws Exception {

        BoardRequestDto request = new BoardRequestDto();
        BoardResponseDto response = new BoardResponseDto();
        response.setBoardId(1);

        when(boardService.createBoard(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/boards/create")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(1));
    }

    @Test
    void createBoard_withInvalidRequest_returns400() throws Exception {

        when(boardService.createBoard(any(), any()))
                .thenThrow(new RuntimeException("Invalid"));

        mockMvc.perform(post("/api/v1/boards/create")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateBoard_withValidData_returns201() throws Exception {

        BoardUpdateRequestDto request = new BoardUpdateRequestDto();
        BoardResponseDto response = new BoardResponseDto();
        response.setBoardId(1);

        when(boardService.updateBoard(any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/boards/update/1")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardId").value(1));
    }

    @Test
    void updateBoard_withWrongId_returns400() throws Exception {

        when(boardService.updateBoard(any(), any(), any()))
                .thenThrow(new RuntimeException("Board not found"));

        mockMvc.perform(put("/api/v1/boards/update/99")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteBoard_withValidId_returns202() throws Exception {

        doNothing().when(boardService).deleteBoard(1, 1);

        mockMvc.perform(delete("/api/v1/boards/delete/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Board deleted successfully"));
    }

    @Test
    void deleteBoard_withWrongId_returns400() throws Exception {

        doNothing().when(boardService).deleteBoard(99, 1);

        mockMvc.perform(delete("/api/v1/boards/delete/99")
                        .header("X-User-Id", 1))
                .andExpect(status().isAccepted());
    }

    @Test
    void getBoardById_withValidId_returns200() throws Exception {

        BoardResponseDto response = new BoardResponseDto();
        response.setBoardId(1);

        when(boardService.getBoardById(1, 1))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/boards/get/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(1));
    }

    @Test
    void getBoardById_withWrongId_returns400() throws Exception {

        when(boardService.getBoardById(99, 1))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/v1/boards/get/99")
                        .header("X-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void closeBoard_withValidId_returns202() throws Exception {

        doNothing().when(boardService).closeBoard(1, 1);

        mockMvc.perform(put("/api/v1/boards/1/close")
                        .header("X-User-Id", 1))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Board closed successfully"));
    }

    @Test
    void closeBoard_withWrongId_returns400() throws Exception {

        mockMvc.perform(put("/api/v1/boards/99/close")
                        .header("X-User-Id", 1))
                .andExpect(status().isAccepted());
    }

    @Test
    void openBoard_withValidId_returns202() throws Exception {

        doNothing().when(boardService).openBoard(1, 1);

        mockMvc.perform(put("/api/v1/boards/1/open")
                        .header("X-User-Id", 1))
                .andExpect(status().isAccepted())
                .andExpect(content().string("Board opened successfully"));
    }

    @Test
    void openBoard_withWrongId_returns400() throws Exception {

        mockMvc.perform(put("/api/v1/boards/99/open")
                        .header("X-User-Id", 1))
                .andExpect(status().isAccepted());
    }

    @Test
    void getWorkspaceId_withValidId_returns200() throws Exception {

        when(boardService.getWorkspaceId(1))
                .thenReturn(10);

        mockMvc.perform(get("/api/v1/boards/workspace/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void getWorkspaceId_withWrongId_returns400() throws Exception {

        when(boardService.getWorkspaceId(99))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/v1/boards/workspace/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isPrivate_withValidId_returns200() throws Exception {

        when(boardService.isPrivate(1))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/boards/is-private/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isPrivate_withWrongId_returns400() throws Exception {

        when(boardService.isPrivate(99))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/v1/boards/is-private/99"))
                .andExpect(status().isBadRequest());
    }
}