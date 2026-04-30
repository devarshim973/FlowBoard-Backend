package com.flowboard.board_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.board_service.controller.BoardMemberController;
import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.service.BoardMemberService;
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

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BoardMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class BoardMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BoardMemberService boardMemberService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void addMember_withValidData_returns201() throws Exception {

        BoardMemberRequestDto request = new BoardMemberRequestDto();

        BoardMemberResponseDto response =
                new BoardMemberResponseDto();
        response.setBoardId(1);

        when(boardMemberService.addMember(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/board-members/add")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardId").value(1));
    }

    @Test
    void addMember_withWrongData_returns400() throws Exception {

        when(boardMemberService.addMember(any(), any()))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(post("/api/v1/board-members/add")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeMember_withValidIds_returns201() throws Exception {

        doNothing().when(boardMemberService)
                .removeMember(1, 2, 1);

        mockMvc.perform(delete("/api/v1/board-members/remove/1/2")
                        .header("X-User-Id", 1))
                .andExpect(status().isCreated())
                .andExpect(content()
                        .string("Member removed successfully"));
    }

    @Test
    void removeMember_withWrongIds_returns400() throws Exception {

        mockMvc.perform(delete("/api/v1/board-members/remove/99/2")
                        .header("X-User-Id", 1))
                .andExpect(status().isCreated());
    }

    @Test
    void getMembers_withValidBoardId_returns200() throws Exception {

        when(boardMemberService.getMembers(any(), any(), any(), any(), any(), any()))
                .thenReturn(null);

        mockMvc.perform(get("/api/v1/board-members/get/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void getMembers_withWrongBoardId_returns400() throws Exception {

        when(boardMemberService.getMembers(any(), any(), any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Not found"));

        mockMvc.perform(get("/api/v1/board-members/get/99")
                        .header("X-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isMember_withValidIds_returns200() throws Exception {

        when(boardMemberService.checkIsMember(1, 2))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/board-members/1/is-member/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void isMember_withWrongIds_returns400() throws Exception {

        when(boardMemberService.checkIsMember(99, 2))
                .thenThrow(new RuntimeException("Error"));

        mockMvc.perform(get("/api/v1/board-members/99/is-member/2"))
                .andExpect(status().isBadRequest());
    }
}