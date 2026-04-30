package com.flowboard.card_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.card_service.controller.CardController;
import com.flowboard.card_service.dto.CardRequestDto;
import com.flowboard.card_service.dto.CardResponseDto;
import com.flowboard.card_service.dto.CardUpdateDto;
import com.flowboard.card_service.entity.Priority;
import com.flowboard.card_service.entity.Status;
import com.flowboard.card_service.service.CardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doNothing;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CardController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardService cardService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createCard_withValidData_returns201() throws Exception {

        CardRequestDto request = new CardRequestDto();

        CardResponseDto response = new CardResponseDto();
        response.setCardId(1);

        when(cardService.createCard(any(), any()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/cards/create")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cardId").value(1));
    }

    @Test
    void createCard_withWrongData_returns400() throws Exception {

        when(cardService.createCard(any(), any()))
                .thenThrow(new RuntimeException());

        mockMvc.perform(post("/api/v1/cards/create")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCard_withValidId_returns200() throws Exception {

        CardResponseDto response = new CardResponseDto();
        response.setCardId(1);

        when(cardService.getCardById(1, 1))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/cards/get/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(1));
    }

    @Test
    void getCard_withWrongId_returns400() throws Exception {

        when(cardService.getCardById(99, 1))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/cards/get/99")
                        .header("X-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCardsByList_withValidId_returns200() throws Exception {

        when(cardService.getCardsByList(1, 1))
                .thenReturn(List.of(new CardResponseDto()));

        mockMvc.perform(get("/api/v1/cards/get/list/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void getCardsByBoard_withValidId_returns200() throws Exception {

        when(cardService.getCardsByBoard(1, 1))
                .thenReturn(List.of(new CardResponseDto()));

        mockMvc.perform(get("/api/v1/cards/get/board/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void updateCard_withValidData_returns200() throws Exception {

        CardUpdateDto request = new CardUpdateDto();

        CardResponseDto response = new CardResponseDto();
        response.setCardId(1);

        when(cardService.updateCard(any(), any(), any()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/cards/update/1")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(1));
    }

    @Test
    void deleteCard_withValidId_returns200() throws Exception {

        doNothing().when(cardService)
                .deleteCard(1, 1);

        mockMvc.perform(delete("/api/v1/cards/delete/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted successfully"));
    }

    @Test
    void moveCard_withValidData_returns200() throws Exception {

        CardResponseDto response = new CardResponseDto();
        response.setCardId(1);

        when(cardService.moveCard(1, 2, 1, 1))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/cards/1/move")
                        .header("X-User-Id", 1)
                        .param("targetListId", "2")
                        .param("position", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.cardId").value(1));
    }

    @Test
    void reorderCards_withValidData_returns200() throws Exception {

        doNothing().when(cardService)
                .reorderCards(any(), any(), any());

        mockMvc.perform(put("/api/v1/cards/list/1/reorder")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("[1,2,3]"))
                .andExpect(status().isOk())
                .andExpect(content().string("Reordered successfully"));
    }

    @Test
    void assignCard_withValidData_returns200() throws Exception {

        CardResponseDto response = new CardResponseDto();
        response.setCardId(1);

        when(cardService.assignCard(1, 2, 1))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/cards/1/assign")
                        .header("X-User-Id", 1)
                        .param("assigneeId", "2"))
                .andExpect(status().isOk());
    }

    @Test
    void updatePriority_withValidData_returns200() throws Exception {

        when(cardService.updatePriority(1, Priority.HIGH, 1))
                .thenReturn(new CardResponseDto());

        mockMvc.perform(put("/api/v1/cards/1/priority")
                        .header("X-User-Id", 1)
                        .param("priority", "HIGH"))
                .andExpect(status().isOk());
    }

    @Test
    void updateStatus_withValidData_returns200() throws Exception {

        when(cardService.updateStatus(1, Status.DONE, 1))
                .thenReturn(new CardResponseDto());

        mockMvc.perform(put("/api/v1/cards/1/status")
                        .header("X-User-Id", 1)
                        .param("status", "DONE"))
                .andExpect(status().isOk());
    }

    @Test
    void archiveCard_withValidId_returns200() throws Exception {

        doNothing().when(cardService)
                .archiveCard(1, 1);

        mockMvc.perform(put("/api/v1/cards/1/archive")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("Archived"));
    }

    @Test
    void unarchiveCard_withValidId_returns200() throws Exception {

        doNothing().when(cardService)
                .unarchiveCard(1, 1);

        mockMvc.perform(put("/api/v1/cards/1/unarchive")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("Unarchived"));
    }

    @Test
    void getOverdueCards_withValidUser_returns200() throws Exception {

        when(cardService.getOverdueCards(1))
                .thenReturn(List.of(new CardResponseDto()));

        mockMvc.perform(get("/api/v1/cards/overdue")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk());
    }
}