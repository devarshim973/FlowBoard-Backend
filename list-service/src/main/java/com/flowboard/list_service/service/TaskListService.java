package com.flowboard.list_service.service;

import com.flowboard.list_service.dto.TaskListOrderRequestDto;
import com.flowboard.list_service.dto.TaskListRequestDto;
import com.flowboard.list_service.dto.TaskListResponseDto;
import com.flowboard.list_service.dto.TaskListUpdateDto;

import java.util.List;

public interface TaskListService {
    public TaskListResponseDto createTaskList(TaskListRequestDto taskListRequestDto, Integer userId);

    public TaskListResponseDto getTaskListById(Integer taskListId, Integer userId);

    public List<TaskListResponseDto> getTaskListByBoard(Integer boardId, Integer userId);

    public TaskListResponseDto updateTaskList(TaskListUpdateDto taskListUpdateDto, Integer taskListId, Integer userId);

    List<TaskListResponseDto> reorderTaskList(Integer boardId, Integer userId, List<TaskListOrderRequestDto> taskListOrder);

    public void archiveTaskList(Integer taskListId, Integer userId);

    public void unarchiveTaskList(Integer taskListId, Integer userId);

    public void deleteTaskList(Integer taskListId, Integer userId);

    public List<TaskListResponseDto> getArchiveTaskLists(Integer boardId, Integer userId);

    public List<TaskListResponseDto> getPublicTaskList(Integer boardId);

    Integer getBoardId(Integer listId);
}
