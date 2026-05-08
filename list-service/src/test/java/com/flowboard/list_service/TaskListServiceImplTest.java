package com.flowboard.list_service;

import com.flowboard.list_service.client.BoardClient;
import com.flowboard.list_service.client.WorkspaceClient;
import com.flowboard.list_service.dto.TaskListOrderRequestDto;
import com.flowboard.list_service.dto.TaskListRequestDto;
import com.flowboard.list_service.dto.TaskListResponseDto;
import com.flowboard.list_service.entity.TaskList;
import com.flowboard.list_service.exception.IllegalOperationException;
import com.flowboard.list_service.exception.TaskListNotFoundException;
import com.flowboard.list_service.mapper.Mapper;
import com.flowboard.list_service.mapper.impl.TaskListRequestMapper;
import com.flowboard.list_service.mapper.impl.TaskListResponseMapper;
import com.flowboard.list_service.repository.TaskListRepository;
import com.flowboard.list_service.service.impl.TaskListServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskListServiceImplTest {

    @Mock
    private TaskListRepository taskListRepository;

    @Mock
    private TaskListRequestMapper taskListRequestMapper;

    @Mock
    private TaskListResponseMapper taskListResponseMapper;

    @Mock
    private BoardClient boardClient;

    @Mock
    private WorkspaceClient workspaceClient;

    @InjectMocks
    private TaskListServiceImpl taskListService;

    private TaskList getList() {
        TaskList t = new TaskList();
        t.setListId(1);
        t.setBoardId(1);
        t.setPosition(1);
        t.setArchived(false);
        return t;
    }

    @Test
    void createTaskList_positive() {

        Integer userId = 1;

        TaskListRequestDto dto = new TaskListRequestDto();
        dto.setBoardId(10);
        dto.setName("Todo");

        TaskList entity = new TaskList();
        entity.setBoardId(10);
        entity.setName("Todo");

        TaskList saved = new TaskList();
        saved.setListId(1);
        saved.setBoardId(10);
        saved.setName("Todo");
        saved.setPosition(1);

        TaskListResponseDto response = new TaskListResponseDto();
        response.setListId(1);
        response.setName("Todo");

        when(boardClient.isPrivate(10)).thenReturn(false);
        when(boardClient.getWorkspaceId(10)).thenReturn(100);
        when(workspaceClient.isMember(100, 1)).thenReturn(true);

        when(taskListRequestMapper.mapTo(dto)).thenReturn(entity);

        when(taskListRepository.maxPosition(10)).thenReturn(0);

        when(taskListRepository.save(entity)).thenReturn(saved);

        when(taskListResponseMapper.mapTo(saved)).thenReturn(response);

        TaskListResponseDto result =
                taskListService.createTaskList(dto, userId);

        assertNotNull(result);
        assertEquals("Todo", result.getName());

        verify(taskListRepository).save(entity);
    }

    @Test
    void createTaskList_negative() {

        TaskListRequestDto dto = new TaskListRequestDto();
        dto.setBoardId(1);

        when(boardClient.isPrivate(1)).thenReturn(false);
        when(boardClient.getWorkspaceId(1)).thenReturn(10);
        when(workspaceClient.isMember(10, 1)).thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> taskListService.createTaskList(dto, 1));
    }

    @Test
    void getTaskListById_positive() {

        TaskList list = getList();

        when(taskListRepository.findById(1))
                .thenReturn(Optional.of(list));

        when(boardClient.isPrivate(1)).thenReturn(false);
        when(boardClient.getWorkspaceId(1)).thenReturn(10);
        when(workspaceClient.isPrivate(10)).thenReturn(false);

        taskListService.getTaskListById(1, 1);
    }

    @Test
    void getTaskListById_negative() {

        when(taskListRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(TaskListNotFoundException.class,
                () -> taskListService.getTaskListById(99, 1));
    }

    @Test
    void reorderTaskList_positive() {

        TaskList list = getList();

        TaskListOrderRequestDto dto = new TaskListOrderRequestDto();
        dto.setTaskListId(1);
        dto.setPosition(1);

        when(boardClient.isPrivate(1)).thenReturn(false);
        when(boardClient.getWorkspaceId(1)).thenReturn(10);
        when(workspaceClient.isMember(10, 1)).thenReturn(true);

        when(taskListRepository
                .findByBoardIdAndArchivedFalseOrderByPosition(1))
                .thenReturn(new ArrayList<>(List.of(list)));

        assertEquals(1,
                taskListService.reorderTaskList(
                        1,
                        1,
                        List.of(dto)
                ).size());
    }

    @Test
    void reorderTaskList_negative() {

        TaskList list = getList();

        TaskListOrderRequestDto dto = new TaskListOrderRequestDto();
        dto.setTaskListId(1);
        dto.setPosition(5);

        when(boardClient.isPrivate(1)).thenReturn(false);
        when(boardClient.getWorkspaceId(1)).thenReturn(10);
        when(workspaceClient.isMember(10, 1)).thenReturn(true);

        when(taskListRepository
                .findByBoardIdAndArchivedFalseOrderByPosition(1))
                .thenReturn(List.of(list));

        assertThrows(IllegalOperationException.class,
                () -> taskListService.reorderTaskList(
                        1,
                        1,
                        List.of(dto)
                ));
    }

    @Test
    void getPublicTaskList_positive() {

        when(boardClient.getWorkspaceId(1)).thenReturn(10);
        when(workspaceClient.isPrivate(10)).thenReturn(false);
        when(boardClient.isPrivate(1)).thenReturn(false);

        when(taskListRepository
                .findByBoardIdAndArchivedFalseOrderByPosition(1))
                .thenReturn(List.of(getList()));

        assertEquals(1,
                taskListService.getPublicTaskList(1).size());
    }

    @Test
    void getPublicTaskList_negative() {

        when(boardClient.getWorkspaceId(1)).thenReturn(10);
        when(workspaceClient.isPrivate(10)).thenReturn(true);

        assertThrows(IllegalOperationException.class,
                () -> taskListService.getPublicTaskList(1));
    }
}