package com.flowboard.workspace_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.workspace_service.controller.WorkspaceController;
import com.flowboard.workspace_service.dto.WorkspaceRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.entity.Visibility;
import com.flowboard.workspace_service.service.WorkspaceService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(WorkspaceController.class)
@AutoConfigureMockMvc(addFilters = false)
class WorkspaceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private WorkspaceService workspaceService;

    @Autowired
    private ObjectMapper objectMapper;

    private CustomPageResponse<WorkspaceResponseDto> getPage() {
        return new CustomPageResponse<>();
    }

    @Test
    void handleCreate_positive() throws Exception {

        WorkspaceRequestDto request =
                new WorkspaceRequestDto();

        request.setName("Workspace One");
        request.setDescription("Demo workspace");
        request.setLogoUrl("logo.png");
        request.setVisibility(Visibility.PUBLIC);

        WorkspaceResponseDto response =
                new WorkspaceResponseDto();
        response.setWorkspaceId(1);

        when(workspaceService.createWorkspace(any(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/workspaces/create")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.workspaceId").value(1));
    }

    @Test
    void handleUpdate_positive() throws Exception {

        WorkspaceRequestDto request =
                new WorkspaceRequestDto();

        request.setName("Updated Workspace");
        request.setDescription("Updated Desc");
        request.setLogoUrl("newlogo.png");
        request.setVisibility(Visibility.PRIVATE);

        WorkspaceResponseDto response =
                new WorkspaceResponseDto();
        response.setWorkspaceId(1);

        when(workspaceService.updateWorkspace(
                anyInt(), any(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/workspaces/update/1")
                        .header("X-User-Id", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workspaceId").value(1));
    }

    @Test
    void handleDelete_positive() throws Exception {

        doNothing().when(workspaceService)
                .deleteWorkspace(1, 1);

        mockMvc.perform(delete("/api/v1/workspaces/delete/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Deleted successfully"));
    }

    @Test
    void handleMyWorkspaces_positive() throws Exception {

        when(workspaceService.getMyWorkspaces(
                anyInt(), anyInt(), anyInt(),
                anyString(), anyString()))
                .thenReturn(getPage());

        mockMvc.perform(get("/api/v1/workspaces/me")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void handleJoinedWorkspaces_positive() throws Exception {

        when(workspaceService.getJoinedWorkspaces(
                anyInt(), anyInt(), anyInt(),
                anyString(), anyString()))
                .thenReturn(getPage());

        mockMvc.perform(get("/api/v1/workspaces/joined")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void getById_positive() throws Exception {

        WorkspaceResponseDto response =
                new WorkspaceResponseDto();
        response.setWorkspaceId(1);

        when(workspaceService.findById(1, 1))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/workspaces/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workspaceId").value(1));
    }

    @Test
    void checkAccess_positive() throws Exception {

        when(workspaceService.checkModificationAccess(1, 1))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/workspaces/access/1")
                        .header("X-User-Id", "1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void handleGetOwnerId_positive() throws Exception {

        when(workspaceService.getOwenerId(1))
                .thenReturn(10);

        mockMvc.perform(get("/api/v1/workspaces/owner/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void handleGetPublicWorkspace_positive() throws Exception {

        when(workspaceService.getPublicWorkspace(
                anyInt(), anyInt(),
                anyString(), anyString()))
                .thenReturn(getPage());

        mockMvc.perform(get("/api/v1/workspaces/public"))
                .andExpect(status().isOk());
    }

    @Test
    void handleIsMember_positive() throws Exception {

        when(workspaceService.isMember(1, 2))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/workspaces/1/member/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void handleIsPrivate_positive() throws Exception {

        when(workspaceService.isPrivate(1))
                .thenReturn(true);

        mockMvc.perform(get("/api/v1/workspaces/private/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void missingHeader_negative() throws Exception {

        mockMvc.perform(get("/api/v1/workspaces/me"))
                .andExpect(status().isBadRequest());
    }
}