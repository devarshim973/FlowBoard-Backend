package com.flowboard.card_service;

import com.flowboard.card_service.controller.CardActivityController;
import com.flowboard.card_service.dto.CardActivityResponseDto;
import com.flowboard.card_service.service.CardActivityService;
import com.flowboard.card_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CardActivityController.class)
@AutoConfigureMockMvc(addFilters = false)
class CardActivityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CardActivityService cardActivityService;

    @Test
    void getByCard_withValidData_returns200() throws Exception {

        PageImpl<CardActivityResponseDto> page =
                new PageImpl<>(
                        List.of(new CardActivityResponseDto()),
                        PageRequest.of(0, 5),
                        1
                );

        CustomPageResponse<CardActivityResponseDto> response =
                new CustomPageResponse<>(page);

        when(cardActivityService.getActivitiesByCard(
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString(),
                anyInt()
        )).thenReturn(response);

        mockMvc.perform(get("/api/v1/cards/card/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void getByCard_withWrongCardId_returns400() throws Exception {

        when(cardActivityService.getActivitiesByCard(
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString(),
                anyInt()
        )).thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/cards/card/99")
                        .header("X-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getByCard_withoutHeader_returns400() throws Exception {

        mockMvc.perform(get("/api/v1/cards/card/1"))
                .andExpect(status().isBadRequest());
    }
}