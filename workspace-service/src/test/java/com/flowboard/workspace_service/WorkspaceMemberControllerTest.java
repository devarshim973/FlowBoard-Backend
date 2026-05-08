package com.flowboard.workspace_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.workspace_service.controller.WorkspaceMemberController;
import com.flowboard.workspace_service.dto.WorkspaceMemberRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceMemberResponseDto;
import com.flowboard.workspace_service.service.WorkspaceMemberService;
import com.flowboard.workspace_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceMemberController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceMemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceMemberService memberService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomPageResponse<WorkspaceMemberResponseDto> getPage() {
        return new CustomPageResponse<>();
    }

    @Test
    void addMember_positive() throws Exception {

        WorkspaceMemberRequestDto request =
                new WorkspaceMemberRequestDto();
        request.setWorkspaceId(1);
        request.setUserId(2);

        WorkspaceMemberResponseDto response =
                new WorkspaceMemberResponseDto();
        response.setWorkspaceId(1);

        when(memberService.addMember(any(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/workspaces/add")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workspaceId").value(1));
    }

    @Test
    void addMember_negative() throws Exception {

        mockMvc.perform(post("/api/v1/workspaces/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void removeMember_positive() throws Exception {

        doNothing().when(memberService)
                .removeMember(1, 2, 1);

        mockMvc.perform(delete("/api/v1/workspaces/1/members/2")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Member removed successfully"));
    }

    @Test
    void removeMember_negative() throws Exception {

        mockMvc.perform(delete("/api/v1/workspaces/1/members/2"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getMembers_positive() throws Exception {

        when(memberService.getMembers(
                anyInt(),
                anyInt(),
                anyInt(),
                anyInt(),
                anyString(),
                anyString()))
                .thenReturn(getPage());

        mockMvc.perform(get("/api/v1/workspaces/1/members")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getMembers_negative() throws Exception {

        mockMvc.perform(get("/api/v1/workspaces/1/members"))
                .andExpect(status().isBadRequest());
    }
}