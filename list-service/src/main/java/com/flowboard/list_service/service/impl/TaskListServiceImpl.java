package com.flowboard.list_service.service.impl;

import com.flowboard.list_service.client.BoardClient;
import com.flowboard.list_service.client.WorkspaceClient;
import com.flowboard.list_service.dto.TaskListOrderRequestDto;
import com.flowboard.list_service.dto.TaskListRequestDto;
import com.flowboard.list_service.dto.TaskListResponseDto;
import com.flowboard.list_service.dto.TaskListUpdateDto;
import com.flowboard.list_service.entity.TaskList;
import com.flowboard.list_service.exception.IllegalOperationException;
import com.flowboard.list_service.exception.TaskListNotFoundException;
import com.flowboard.list_service.mapper.Mapper;
import com.flowboard.list_service.mapper.impl.TaskListRequestMapper;
import com.flowboard.list_service.mapper.impl.TaskListResponseMapper;
import com.flowboard.list_service.repository.TaskListRepository;
import com.flowboard.list_service.service.TaskListService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;


/*
Workspace PUBLIC + Board PUBLIC
→ Anyone (even guest) can view and crud by only workspace member

Workspace PUBLIC + Board PRIVATE
→ board members can view and crud

Workspace PRIVATE + Board PUBLIC
→ workspace members can view and crud

Workspace PRIVATE + Board PRIVATE
→ board members can view and crud

 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TaskListServiceImpl implements TaskListService {
    private final TaskListRepository taskListRepository;
    private final TaskListRequestMapper taskListRequestMapper;
    private final TaskListResponseMapper taskListResponseMapper;
    private final BoardClient boardClient;
    private final WorkspaceClient workspaceClient;

    /*
    Creation allowed if you are member or the workspace is public, board is public and
    you are a member of workspace
     */
    @Override
    public TaskListResponseDto createTaskList(TaskListRequestDto taskListRequestDto, Integer userId) {
        Integer boardId = taskListRequestDto.getBoardId();
        log.info("Create list requested for board {} by user {}", boardId, userId);
        validateMakeChangesRequest(boardId, userId);

        TaskList taskList = taskListRequestMapper.mapTo(taskListRequestDto);
        Integer position = getLastPosition(boardId);
        taskList.setPosition(position + 1);

        TaskList savedList = taskListRepository.save(taskList);
        log.info("List created with id {} in board {}", savedList.getListId(), boardId);

        return taskListResponseMapper.mapTo(savedList);
    }

    /*
    View board is allowed if you are member of board or if workspace is public and board
    is public or the workspace is private and board is public and you are member
     */
    @Override
    public TaskListResponseDto getTaskListById(Integer taskListId, Integer userId) {
        TaskList taskList = getTaskList(taskListId);
        Integer boardId = taskList.getBoardId();

        validateViewRequest(boardId, userId);

        return taskListResponseMapper.mapTo(taskList);
    }

    @Override
    public List<TaskListResponseDto> getTaskListByBoard(Integer boardId, Integer userId) {
        validateViewRequest(boardId, userId);

        List<TaskList> taskLists = taskListRepository.findByBoardIdAndArchivedFalseOrderByPosition(boardId);

        return taskLists.stream()
                .map(taskListResponseMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public TaskListResponseDto updateTaskList(TaskListUpdateDto taskListUpdateDto, Integer taskListId, Integer userId) {
        log.info("Update list requested for list {} by user {}", taskListId, userId);
        TaskList savedTaskList = getTaskList(taskListId);
        Integer boardId = savedTaskList.getBoardId();

        validateMakeChangesRequest(boardId, userId);

        savedTaskList.setName(taskListUpdateDto.getName());
        savedTaskList.setColor(taskListUpdateDto.getColor());

        TaskList updatedTaskList = taskListRepository.save(savedTaskList);
        log.info("List updated with id {}", updatedTaskList.getListId());

        return taskListResponseMapper.mapTo(updatedTaskList);
    }

    @Override
    @Transactional
    public List<TaskListResponseDto> reorderTaskList(Integer boardId, Integer userId, List<TaskListOrderRequestDto> taskListOrder) {
        log.info("List reorder requested for board {} by user {}", boardId, userId);
        validateMakeChangesRequest(boardId, userId);

        List<TaskList> existing = taskListRepository.findByBoardIdAndArchivedFalseOrderByPosition(boardId);
        Map<Integer, TaskList> map = existing.stream()
                .collect(Collectors.toMap(TaskList::getListId, t -> t));

        int totalList = existing.size();

        HashMap<Integer, Integer> newOrder = new HashMap<>();
        HashSet<Integer> positions = new HashSet<>();

        for (TaskListOrderRequestDto dto : taskListOrder) {
            Integer position = dto.getPosition();
            Integer taskListId = dto.getTaskListId();

            if (position < 1 || position > totalList) {
                throw new IllegalOperationException("Invalid position for a list");
            }

            if(!map.containsKey(taskListId)) {
                throw new IllegalOperationException("Invalid task list id " + taskListId.toString());
            }

            TaskList taskList = map.get(taskListId);

            if (!taskList.getBoardId().equals(boardId)) {
                throw new IllegalOperationException("List does not belong to this board");
            }

            newOrder.put(taskListId, position);
            positions.add(position);
        }

        if (totalList != newOrder.size() || totalList != positions.size()) {
            throw new IllegalOperationException("The total number of list and positions are incorrect");
        }

        for (TaskList taskList : map.values()) {
            taskList.setPosition(newOrder.get(taskList.getListId()));
        }

        /*
        Using save all is always a better idea as it make only 1 call to db so increases
        speed very effectively.
         */
        taskListRepository.saveAll(existing);

        List<TaskList> resultantOrder =
                taskListRepository.findByBoardIdAndArchivedFalseOrderByPosition(boardId);
        log.info("Lists reordered for board {}", boardId);

        return resultantOrder.stream()
                .map(taskListResponseMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public void archiveTaskList(Integer taskListId, Integer userId) {
        log.info("Archive list requested for list {} by user {}", taskListId, userId);
        TaskList taskList = getTaskList(taskListId);
        Integer boardId = taskList.getBoardId();

        validateMakeChangesRequest(boardId, userId);

        taskList.setArchived(true);

        taskListRepository.save(taskList);
        log.info("List archived with id {}", taskListId);
    }

    @Override
    public void unarchiveTaskList(Integer taskListId, Integer userId) {
        log.info("Unarchive list requested for list {} by user {}", taskListId, userId);
        TaskList taskList = getTaskList(taskListId);
        Integer boardId = taskList.getBoardId();

        validateMakeChangesRequest(boardId, userId);

        taskList.setArchived(false);

        taskListRepository.save(taskList);
        log.info("List unarchived with id {}", taskListId);
    }

    @Override
    public void deleteTaskList(Integer taskListId, Integer userId) {
        log.info("Delete list requested for list {} by user {}", taskListId, userId);
        TaskList taskList = getTaskList(taskListId);
        Integer boardId = taskList.getBoardId();

        validateMakeChangesRequest(boardId, userId);

        taskList.setArchived(true);
        taskListRepository.save(taskList);
        log.info("List deleted with id {}", taskListId);
    }

    @Override
    public List<TaskListResponseDto> getArchiveTaskLists(Integer boardId, Integer userId) {
        validateViewRequest(boardId, userId);

        List<TaskList> archivedTaskLists = taskListRepository.findByBoardIdAndArchivedTrueOrderByPosition(boardId);

        return archivedTaskLists
                .stream()
                .map(taskListResponseMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public List<TaskListResponseDto> getPublicTaskList(Integer boardId) {
        Integer workspaceId = boardClient.getWorkspaceId(boardId);
        if(workspaceClient.isPrivate(workspaceId) || boardClient.isPrivate(boardId)) {
            throw new IllegalOperationException("You cannot view this task list");
        }

        List<TaskList> taskLists = taskListRepository
                .findByBoardIdAndArchivedFalseOrderByPosition(boardId);

        return taskLists.stream()
                .map(taskListResponseMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public Integer getBoardId(Integer listId) {
        return getTaskList(listId).getBoardId();
    }

    private Integer getLastPosition(Integer boardId) {
        return taskListRepository.maxPosition(boardId);
    }

    private TaskList getTaskList(Integer taskListId) {
        return taskListRepository.findById(taskListId)
                .orElseThrow(() -> new TaskListNotFoundException("Task List not found with id " + taskListId.toString()));
    }

    /*
    if both the if conditions are false this means public workspace and public board
    here we are making 3 calls to board service in worst case and 2 calls to workspace
    service a better idea is to just make a single call and create a dto to take all
    the things which to want in return for optimization(to do)
     */
    private void validateViewRequest(Integer boardId, Integer userId) {
        /*
         If board is private user must be member
        */
        if(boardClient.isPrivate(boardId)) {
            if(boardClient.isMember(boardId, userId)) return;
            throw new IllegalOperationException("You are not allowed to view this list");
        }

        /*
        If we are here means board is public so now if workspace is private you must
         be member of workspace
         */
        Integer workspaceId = boardClient.getWorkspaceId(boardId);
        if(workspaceClient.isPrivate(workspaceId)) {
            if(workspaceClient.isMember(workspaceId, userId)) return;
            throw new IllegalOperationException("You are not allowed to view this list");
        }

        // public board + public workspace -> allowed
        return;
    }

    /*
    Look at this how badly this is optimized this is making 3 calls to board service
    a better idea is to create a dto to take all the 3 things and just make a single
    call to board service for improvements -> to do if have time
     */
    private void validateMakeChangesRequest(Integer boardId, Integer userId) {
        /*
         If board is private user must be member
        */
        if(boardClient.isPrivate(boardId)) {
            if(boardClient.isMember(boardId, userId)) return;
            throw new IllegalOperationException("You are not allowed to modify this list");
        }

        /*
         If we are here means board is public so now must be member to make changes
        */
        Integer workspaceId = boardClient.getWorkspaceId(boardId);
        if(!workspaceClient.isMember(workspaceId, userId)){
            throw new IllegalOperationException("You are not allowed to modify this list");
        }
    }
}
