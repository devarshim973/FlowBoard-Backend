package com.flowboard.workspace_service.controller;

import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.service.WorkspaceService;
import com.flowboard.workspace_service.util.AppConstants;
import com.flowboard.workspace_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/workspaces")
@RequiredArgsConstructor
public class AdminWorkspaceController {
    private final WorkspaceService workspaceService;

    @GetMapping
    public ResponseEntity<CustomPageResponse<WorkspaceResponseDto>> getAllWorkspaces(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
            @RequestParam(name = "sort", defaultValue = AppConstants.sortForWorkspace) String by,
            @RequestParam(name = "direction", defaultValue = AppConstants.direction) String direction) {
        ensureAdmin(role);
        return ResponseEntity.ok(workspaceService.getAllWorkspaces(page, size, by, direction));
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<String> deleteWorkspace(@RequestHeader("X-User-Role") String role,
                                                  @PathVariable("workspaceId") Integer workspaceId) {
        ensureAdmin(role);
        workspaceService.deleteWorkspaceAsAdmin(workspaceId);
        return ResponseEntity.ok("Workspace deleted successfully");
    }

    private void ensureAdmin(String role) {
        String normalizedRole = role == null ? "" : role.split(",")[0].trim().toUpperCase();
        if (!"PLATFORM_ADMIN".equals(normalizedRole) && !"ADMIN".equals(normalizedRole)) {
            throw new IllegalArgumentException("Admin access required");
        }
    }
}

