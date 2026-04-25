package com.flowboard.workspace_service.controller;

import com.flowboard.workspace_service.dto.WorkspaceRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.service.WorkspaceService;
import com.flowboard.workspace_service.util.AppConstants;
import com.flowboard.workspace_service.util.CustomPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Workspace Controller", description = "Workspace management related APIs")
public class WorkspaceController {

    private final WorkspaceService workspaceService;

    private Integer getUserId(HttpServletRequest request) {
        Integer userId = Integer.parseInt(request.getHeader("X-User-Id"));
        log.info("Id extraction successful");
        return userId;
    }

    @Operation(summary = "Create workspace", description = "Creates a new workspace")
    @ApiResponse(responseCode = "201", description = "Workspace created successfully")
    @PostMapping("/create")
    public ResponseEntity<WorkspaceResponseDto> handleCreate(@RequestBody @Valid WorkspaceRequestDto dto,  HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(workspaceService.createWorkspace(dto, getUserId(request)));
    }

    @Operation(summary = "Update workspace", description = "Updates workspace details")
    @ApiResponse(responseCode = "200", description = "Workspace updated successfully")
    @PutMapping("/update/{id}")
    public ResponseEntity<WorkspaceResponseDto> handleUpdate(@PathVariable Integer id,
                                                             @RequestBody @Valid WorkspaceRequestDto dto,
                                                             HttpServletRequest request) {
        return ResponseEntity.ok(workspaceService.updateWorkspace(id, dto, getUserId(request)));
    }

    @Operation(summary = "Delete workspace", description = "Deletes workspace by ID")
    @ApiResponse(responseCode = "200", description = "Workspace deleted successfully")
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<String> handleDelete(@PathVariable Integer id, HttpServletRequest request) {
        workspaceService.deleteWorkspace(id, getUserId(request));

        return ResponseEntity.ok("Deleted successfully");
    }

    @Operation(summary = "Get my workspaces", description = "Returns paginated workspaces owned by logged user")
    @ApiResponse(responseCode = "200", description = "Workspaces fetched successfully")
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


    @Operation(summary = "Get joined workspaces", description = "Returns paginated workspaces joined by logged user")
    @ApiResponse(responseCode = "200", description = "Joined workspaces fetched successfully")
    @GetMapping("/joined")
    public ResponseEntity<CustomPageResponse<WorkspaceResponseDto>> handleJoinedWorkspaces(
            HttpServletRequest request,
            @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
            @RequestParam(name = "sort", defaultValue = AppConstants.sortForWorkspace) String by,
            @RequestParam(name = "direction", defaultValue = AppConstants.direction) String direction) {

        Integer ownerId = getUserId(request);
        return ResponseEntity.ok(workspaceService.getJoinedWorkspaces(ownerId, page, size, by, direction));
    }

    @Operation(summary = "Get workspace by ID", description = "Returns workspace details")
    @ApiResponse(responseCode = "200", description = "Workspace fetched successfully")
    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceResponseDto> getById(HttpServletRequest request, @PathVariable Integer workspaceId) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(workspaceService.findById(workspaceId, userId));
    }

    @Operation(summary = "Check workspace access", description = "Checks modification access for logged user")
    @ApiResponse(responseCode = "200", description = "Access status fetched successfully")
    @GetMapping("/access/{workspaceId}")
    public ResponseEntity<Boolean> checkAccess(@PathVariable Integer workspaceId, HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.ok(workspaceService.checkModificationAccess(workspaceId, userId));
    }

    /*
    This is for inter service communication, board service uses this
     */
    @Operation(summary = "Get workspace owner ID", description = "Returns owner user ID of workspace")
    @ApiResponse(responseCode = "200", description = "Owner ID fetched successfully")
    @GetMapping("/owner/{id}")
    public Integer handleGetOwnerId(@PathVariable Integer id) {
        return workspaceService.getOwenerId(id);
    }

    @Operation(summary = "Get public workspaces", description = "Returns paginated public workspaces")
    @ApiResponse(responseCode = "200", description = "Public workspaces fetched successfully")
    @GetMapping("/public")
    public ResponseEntity<CustomPageResponse<WorkspaceResponseDto>> handleGetPublicWorkspace(
            @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
            @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
            @RequestParam(name = "sort", defaultValue = AppConstants.sortForWorkspace) String by,
            @RequestParam(name = "direction", defaultValue = AppConstants.direction) String direction) {
        return ResponseEntity.ok(workspaceService.getPublicWorkspace(page, size, by, direction));
    }

    @Operation(summary = "Check workspace membership", description = "Checks whether user is member of workspace")
    @ApiResponse(responseCode = "200", description = "Membership status fetched successfully")
    @GetMapping("/{workspaceId}/member/{memberId}")
    public Boolean handleIsMember(@PathVariable Integer workspaceId,
                                  @PathVariable Integer memberId) {
        return workspaceService.isMember(workspaceId, memberId);
    }

    @Operation(summary = "Check workspace privacy", description = "Returns true if workspace is private")
    @ApiResponse(responseCode = "200", description = "Privacy status fetched successfully")
    @GetMapping("/private/{workspaceId}")
    public Boolean handleIsPrivate(@PathVariable Integer workspaceId) {
        return workspaceService.isPrivate(workspaceId);
    }
}