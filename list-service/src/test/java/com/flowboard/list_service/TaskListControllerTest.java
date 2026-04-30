package com.flowboard.list_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.list_service.controller.TaskListController;
import com.flowboard.list_service.dto.TaskListOrderRequestDto;
import com.flowboard.list_service.dto.TaskListRequestDto;
import com.flowboard.list_service.dto.TaskListResponseDto;
import com.flowboard.list_service.dto.TaskListUpdateDto;
import com.flowboard.list_service.service.TaskListService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskListController.class)
@AutoConfigureMockMvc(addFilters = false)
class TaskListControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TaskListService taskListService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createTaskList_positive() throws Exception {

        TaskListRequestDto request = new TaskListRequestDto();
        TaskListResponseDto response = new TaskListResponseDto();
        response.setListId(1);

        when(taskListService.createTaskList(any(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/lists/create")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.listId").value(1));
    }

    @Test
    void createTaskList_negative() throws Exception {

        mockMvc.perform(post("/api/v1/lists/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTaskListById_positive() throws Exception {

        TaskListResponseDto response = new TaskListResponseDto();
        response.setListId(1);

        when(taskListService.getTaskListById(1, 1))
                .thenReturn(response);

        mockMvc.perform(get("/api/v1/lists/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.listId").value(1));
    }

    @Test
    void getTaskListById_negative() throws Exception {

        when(taskListService.getTaskListById(99, 1))
                .thenThrow(new RuntimeException());

        mockMvc.perform(get("/api/v1/lists/99")
                        .header("X-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getTaskListsByBoard_positive() throws Exception {

        when(taskListService.getTaskListByBoard(1, 1))
                .thenReturn(List.of(new TaskListResponseDto()));

        mockMvc.perform(get("/api/v1/lists/board/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void updateTaskList_positive() throws Exception {

        TaskListUpdateDto request = new TaskListUpdateDto();

        TaskListResponseDto response = new TaskListResponseDto();
        response.setListId(1);

        when(taskListService.updateTaskList(any(), anyInt(), anyInt()))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/lists/update/1")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.listId").value(1));
    }

    @Test
    void reorderTaskLists_positive() throws Exception {

        when(taskListService.reorderTaskList(anyInt(), anyInt(), any()))
                .thenReturn(List.of(new TaskListResponseDto()));

        mockMvc.perform(put("/api/v1/lists/board/1/reorder")
                        .header("X-User-Id", 1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                List.of(new TaskListOrderRequestDto())
                        )))
                .andExpect(status().isAccepted());
    }

    @Test
    void archiveTaskList_positive() throws Exception {

        doNothing().when(taskListService)
                .archiveTaskList(1, 1);

        mockMvc.perform(patch("/api/v1/lists/1/archive")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("List archived successfully"));
    }

    @Test
    void unarchiveTaskList_positive() throws Exception {

        doNothing().when(taskListService)
                .unarchiveTaskList(1, 1);

        mockMvc.perform(patch("/api/v1/lists/1/unarchive")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("List unarchived successfully"));
    }

    @Test
    void deleteTaskList_positive() throws Exception {

        doNothing().when(taskListService)
                .deleteTaskList(1, 1);

        mockMvc.perform(delete("/api/v1/lists/delete/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isAccepted())
                .andExpect(content()
                        .string("Deleted successfully"));
    }

    @Test
    void getArchivedTaskLists_positive() throws Exception {

        when(taskListService.getArchiveTaskLists(1, 1))
                .thenReturn(List.of(new TaskListResponseDto()));

        mockMvc.perform(get("/api/v1/lists/board/1/archived")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk());
    }

    @Test
    void getPublicTaskList_positive() throws Exception {

        when(taskListService.getPublicTaskList(1))
                .thenReturn(List.of(new TaskListResponseDto()));

        mockMvc.perform(get("/api/v1/lists/public/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getBoardId_positive() throws Exception {

        when(taskListService.getBoardId(1))
                .thenReturn(10);

        mockMvc.perform(get("/api/v1/lists/get-boardId/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("10"));
    }

    @Test
    void missingHeader_negative() throws Exception {

        mockMvc.perform(get("/api/v1/lists/1"))
                .andExpect(status().isBadRequest());
    }
}