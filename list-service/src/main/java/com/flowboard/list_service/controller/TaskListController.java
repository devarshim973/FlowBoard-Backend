package com.flowboard.list_service.controller;

import com.flowboard.list_service.dto.TaskListOrderRequestDto;
import com.flowboard.list_service.dto.TaskListRequestDto;
import com.flowboard.list_service.dto.TaskListResponseDto;
import com.flowboard.list_service.dto.TaskListUpdateDto;
import com.flowboard.list_service.service.TaskListService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
@Tag(name = "Task List Controller", description = "Task list management related APIs")
public class TaskListController {
    private final TaskListService taskListService;

    private Integer extractUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        return Integer.parseInt(userIdHeader);
    }

    @Operation(summary = "Create task list", description = "Creates a new task list inside board")
    @ApiResponse(responseCode = "201", description = "Task list created successfully")
    @PostMapping("/create")
    public ResponseEntity<TaskListResponseDto> createTaskList(@RequestBody TaskListRequestDto requestDto,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskListService.createTaskList(requestDto, extractUserId(request)));
    }

    @Operation(summary = "Get task list by ID", description = "Returns task list details")
    @ApiResponse(responseCode = "200", description = "Task list fetched successfully")
    @GetMapping("/{listId}")
    public ResponseEntity<TaskListResponseDto> getTaskListById(@PathVariable Integer listId,
                                                               HttpServletRequest request) {
        return ResponseEntity.ok(taskListService.getTaskListById(listId, extractUserId(request)));
    }

    @Operation(summary = "Get task lists by board", description = "Returns all active task lists of board")
    @ApiResponse(responseCode = "200", description = "Task lists fetched successfully")
    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<TaskListResponseDto>> getTaskListsByBoard(@PathVariable Integer boardId,
                                                                         HttpServletRequest request) {
        return ResponseEntity.ok(taskListService.getTaskListByBoard(boardId, extractUserId(request)));
    }

    @Operation(summary = "Update task list", description = "Updates task list details")
    @ApiResponse(responseCode = "202", description = "Task list updated successfully")
    @PutMapping("/update/{listId}")
    public ResponseEntity<TaskListResponseDto> updateTaskList(@PathVariable Integer listId, @RequestBody TaskListUpdateDto updateDto, HttpServletRequest request) {
        return ResponseEntity.accepted()
                .body(taskListService.updateTaskList(updateDto, listId, extractUserId(request)));
    }

    @Operation(summary = "Reorder task lists", description = "Reorders task lists inside board")
    @ApiResponse(responseCode = "202", description = "Task lists reordered successfully")
    @PutMapping("/board/{boardId}/reorder")
    public ResponseEntity<List<TaskListResponseDto>> reorderTaskLists(@PathVariable Integer boardId, @RequestBody List<TaskListOrderRequestDto> order, HttpServletRequest request) {
        return ResponseEntity.accepted()
                .body(taskListService.reorderTaskList(boardId, extractUserId(request), order));
    }

    @Operation(summary = "Archive task list", description = "Archives task list")
    @ApiResponse(responseCode = "200", description = "Task list archived successfully")
    @PatchMapping("/{listId}/archive")
    public ResponseEntity<String> archiveTaskList(@PathVariable Integer listId, HttpServletRequest request) {
        taskListService.archiveTaskList(listId, extractUserId(request));
            return ResponseEntity.ok().body("List archived successfully");
    }

    @Operation(summary = "Unarchive task list", description = "Restores archived task list")
    @ApiResponse(responseCode = "200", description = "Task list unarchived successfully")
    @PatchMapping("/{listId}/unarchive")
    public ResponseEntity<String> unarchiveTaskList(@PathVariable Integer listId, HttpServletRequest request) {
        taskListService.unarchiveTaskList(listId, extractUserId(request));
        return ResponseEntity.ok().body("List unarchived successfully");
    }

    @Operation(summary = "Delete task list", description = "Deletes task list by ID")
    @ApiResponse(responseCode = "202", description = "Task list deleted successfully")
    @DeleteMapping("/delete/{listId}")
    public ResponseEntity<String> deleteTaskList(@PathVariable Integer listId, HttpServletRequest request) {
        taskListService.deleteTaskList(listId, extractUserId(request));
        return ResponseEntity.accepted().body("Deleted successfully");
    }

    @Operation(summary = "Get archived task lists", description = "Returns archived task lists of board")
    @ApiResponse(responseCode = "200", description = "Archived task lists fetched successfully")
    @GetMapping("/board/{boardId}/archived")
    public ResponseEntity<List<TaskListResponseDto>> getArchivedTaskLists(@PathVariable Integer boardId, HttpServletRequest request) {
        return ResponseEntity.ok(taskListService.getArchiveTaskLists(boardId, extractUserId(request)));
    }

    @Operation(summary = "Get public task lists", description = "Returns public task lists of board")
    @ApiResponse(responseCode = "200", description = "Public task lists fetched successfully")
    @GetMapping("/public/{boardId}")
    public ResponseEntity<List<TaskListResponseDto>> getPublicTaskList(@PathVariable Integer boardId) {
        return ResponseEntity.ok(taskListService.getPublicTaskList(boardId));
    }

    @Operation(summary = "Get board ID by task list", description = "Returns board ID of given task list")
    @ApiResponse(responseCode = "200", description = "Board ID fetched successfully")
    @GetMapping("/get-boardId/{listId}")
    public Integer getBoardId(@PathVariable(value = "listId") Integer listId) {
        return taskListService.getBoardId(listId);
    }
}