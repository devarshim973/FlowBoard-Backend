package com.flowboard.list_service.controller;

import com.flowboard.list_service.dto.TaskListOrderRequestDto;
import com.flowboard.list_service.dto.TaskListRequestDto;
import com.flowboard.list_service.dto.TaskListResponseDto;
import com.flowboard.list_service.dto.TaskListUpdateDto;
import com.flowboard.list_service.service.TaskListService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
public class TaskListController {
    private final TaskListService taskListService;

    private Integer extractUserId(HttpServletRequest request) {
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader == null) {
            throw new RuntimeException("Missing X-User-Id header");
        }

        return Integer.parseInt(userIdHeader);
    }

    @PostMapping("/create")
    public ResponseEntity<TaskListResponseDto> createTaskList(@RequestBody TaskListRequestDto requestDto,
                                                              HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(taskListService.createTaskList(requestDto, extractUserId(request)));
    }

    @GetMapping("/{listId}")
    public ResponseEntity<TaskListResponseDto> getTaskListById(@PathVariable Integer listId,
                                                               HttpServletRequest request) {
        return ResponseEntity.ok(taskListService.getTaskListById(listId, extractUserId(request)));
    }

    @GetMapping("/board/{boardId}")
    public ResponseEntity<List<TaskListResponseDto>> getTaskListsByBoard(@PathVariable Integer boardId,
                                                                         HttpServletRequest request) {
        return ResponseEntity.ok(taskListService.getTaskListByBoard(boardId, extractUserId(request)));
    }

    @PutMapping("/update/{listId}")
    public ResponseEntity<TaskListResponseDto> updateTaskList(@PathVariable Integer listId, @RequestBody TaskListUpdateDto updateDto, HttpServletRequest request) {
        return ResponseEntity.accepted()
                .body(taskListService.updateTaskList(updateDto, listId, extractUserId(request)));
    }

    @PutMapping("/board/{boardId}/reorder")
    public ResponseEntity<List<TaskListResponseDto>> reorderTaskLists(@PathVariable Integer boardId, @RequestBody List<TaskListOrderRequestDto> order, HttpServletRequest request) {
        return ResponseEntity.accepted()
                .body(taskListService.reorderTaskList(boardId, extractUserId(request), order));
    }

    @PatchMapping("/{listId}/archive")
    public ResponseEntity<String> archiveTaskList(@PathVariable Integer listId, HttpServletRequest request) {
        taskListService.archiveTaskList(listId, extractUserId(request));
            return ResponseEntity.ok().body("List archived successfully");
    }

    @PatchMapping("/{listId}/unarchive")
    public ResponseEntity<String> unarchiveTaskList(@PathVariable Integer listId, HttpServletRequest request) {
        taskListService.unarchiveTaskList(listId, extractUserId(request));
        return ResponseEntity.ok().body("List unarchived successfully");
    }

    @DeleteMapping("/delete/{listId}")
    public ResponseEntity<String> deleteTaskList(@PathVariable Integer listId, HttpServletRequest request) {
        taskListService.deleteTaskList(listId, extractUserId(request));
        return ResponseEntity.accepted().body("Deleted successfully");
    }

    @GetMapping("/board/{boardId}/archived")
    public ResponseEntity<List<TaskListResponseDto>> getArchivedTaskLists(@PathVariable Integer boardId, HttpServletRequest request) {
        return ResponseEntity.ok(taskListService.getArchiveTaskLists(boardId, extractUserId(request)));
    }

    @GetMapping("/public/{boardId}")
    public ResponseEntity<List<TaskListResponseDto>> getPublicTaskList(@PathVariable Integer boardId) {
        return ResponseEntity.ok(taskListService.getPublicTaskList(boardId));
    }

    @GetMapping("/get-boardId/{listId}")
    public Integer getBoardId(@PathVariable(value = "listId") Integer listId) {
        return taskListService.getBoardId(listId);
    }
}