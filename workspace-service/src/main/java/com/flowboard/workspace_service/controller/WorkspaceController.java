package com.flowboard.workspace_service.controller;

import com.flowboard.workspace_service.dto.WorkspaceRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.service.WorkspaceService;
import com.flowboard.workspace_service.util.AppConstants;
import com.flowboard.workspace_service.util.CustomPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = Integer.parseInt(request.getHeader("X-User-Id"));
        log.info("Id extraction successful");
        return userId;
    }

    @PostMapping("/create")
    public ResponseEntity<WorkspaceResponseDto> handleCreate(@RequestBody @Valid WorkspaceRequestDto dto,  HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.createWorkspace(dto, getUserId(request)));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<WorkspaceResponseDto> handleUpdate(@PathVariable Integer id,
                                                             @RequestBody @Valid WorkspaceRequestDto dto,
                                                             HttpServletRequest request) {
        return ResponseEntity.ok(workspaceService.updateWorkspace(id, dto, getUserId(request)));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> handleDelete(@PathVariable Integer id, HttpServletRequest request) {
        workspaceService.deleteWorkspace(id, getUserId(request));

        return ResponseEntity.ok("Deleted successfully");
    }

    @GetMapping("/me")
    public ResponseEntity<CustomPageResponse<WorkspaceResponseDto>> handleMyWorkspaces(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
            @RequestParam(name = "sort", defaultValue = AppConstants.sortForWorkspace) String by,
            @RequestParam(name = "direction", defaultValue = AppConstants.direction) String direction) {

        Integer ownerId = getUserId(request);
        return ResponseEntity.ok(workspaceService.getMyWorkspaces(ownerId, page, size, by, direction));
    }
}