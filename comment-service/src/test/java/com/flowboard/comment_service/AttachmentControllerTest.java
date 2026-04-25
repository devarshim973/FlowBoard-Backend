package com.flowboard.comment_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.comment_service.controller.AttachmentController;
import com.flowboard.comment_service.dto.AttachmentResponseDto;
import com.flowboard.comment_service.service.AttachmentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AttachmentController.class)
class AttachmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AttachmentService attachmentService;

    @Autowired
    private ObjectMapper objectMapper;

    // ===============================
    // UPLOAD TESTS
    // ===============================

    @Test
    @DisplayName("upload() positive")
    void upload_positive() throws Exception {

        AttachmentResponseDto response = new AttachmentResponseDto();
        response.setAttachmentId(1);
        response.setFileName("test.pdf");

        MockMultipartFile file =
                new MockMultipartFile(
                        "file",
                        "test.pdf",
                        MediaType.APPLICATION_PDF_VALUE,
                        "dummy".getBytes()
                );

        when(attachmentService.uploadAttachment(
                ArgumentMatchers.any(),
                ArgumentMatchers.any()))
                .thenReturn(response);

        mockMvc.perform(
                        multipart("/api/v1/attachments/upload")
                                .file(file)
                                .param("cardId", "10")
                                .param("uploaderId", "5")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.attachmentId").value(1))
                .andExpect(jsonPath("$.fileName").value("test.pdf"));
    }

    @Test
    @DisplayName("upload() negative - missing file")
    void upload_negative_missingFile() throws Exception {

        mockMvc.perform(
                        multipart("/api/v1/attachments/upload")
                                .param("cardId", "10")
                                .param("uploaderId", "5")
                )
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // GET BY CARD TESTS
    // ===============================

    @Test
    @DisplayName("getByCard() positive")
    void getByCard_positive() throws Exception {

        AttachmentResponseDto dto = new AttachmentResponseDto();
        dto.setAttachmentId(1);
        dto.setFileName("img.png");

        when(attachmentService.getAttachmentsByCard(10))
                .thenReturn(List.of(dto));

        mockMvc.perform(get("/api/v1/attachments/card/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].attachmentId").value(1))
                .andExpect(jsonPath("$[0].fileName").value("img.png"));
    }

    @Test
    @DisplayName("getByCard() negative - invalid cardId")
    void getByCard_negative() throws Exception {

        when(attachmentService.getAttachmentsByCard(999))
                .thenThrow(new RuntimeException("No attachments"));

        mockMvc.perform(get("/api/v1/attachments/card/999"))
                .andExpect(status().isBadRequest());
    }

    // ===============================
    // DELETE TESTS
    // ===============================

    @Test
    @DisplayName("delete() positive")
    void delete_positive() throws Exception {

        doNothing().when(attachmentService).deleteAttachment(1);

        mockMvc.perform(delete("/api/v1/attachments/delete/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Attachment deleted"));
    }

    @Test
    @DisplayName("delete() negative")
    void delete_negative() throws Exception {

        doThrow(new RuntimeException("Not found"))
                .when(attachmentService)
                .deleteAttachment(99);

        mockMvc.perform(delete("/api/v1/attachments/delete/99"))
                .andExpect(status().isBadRequest());
    }
}