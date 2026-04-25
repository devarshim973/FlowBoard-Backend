package com.flowboard.workspace_service.controller;

import com.flowboard.workspace_service.dto.WorkspaceMemberRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceMemberResponseDto;
import com.flowboard.workspace_service.service.WorkspaceMemberService;
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
@Tag(name = "Workspace Member Controller", description = "Workspace member management related APIs")
public class WorkspaceMemberController {

    private final WorkspaceMemberService memberService;

    private Integer getUserId(HttpServletRequest request) {
        return Integer.parseInt(request.getHeader("X-User-Id"));
    }

    @Operation(summary = "Add workspace member", description = "Adds member to workspace")
    @ApiResponse(responseCode = "201", description = "Member added successfully")
    @PostMapping("/add")
    public ResponseEntity<WorkspaceMemberResponseDto> addMember(@RequestBody @Valid WorkspaceMemberRequestDto workspaceMemberRequestDto,
                                                                HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.addMember(workspaceMemberRequestDto, getUserId(request)));
    }

    @Operation(summary = "Remove workspace member", description = "Removes member from workspace")
    @ApiResponse(responseCode = "200", description = "Member removed successfully")
    @DeleteMapping("/{workspaceId}/members/{userId}")
    public ResponseEntity<String> removeMember(@PathVariable Integer workspaceId,
                                               @PathVariable Integer userId,
                                               HttpServletRequest request) {

        log.info("Remove member");
        memberService.removeMember(workspaceId, userId, getUserId(request));
        return ResponseEntity.ok("Member removed successfully");
    }

    @GetMapping("/{workspaceId}/members")
    public ResponseEntity<CustomPageResponse<WorkspaceMemberResponseDto>> getMembers(@PathVariable Integer workspaceId,
                                                                                     HttpServletRequest request,
                                                                                     @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                                     @RequestParam(value = "size", defaultValue = AppConstants.size) int size,
                                                                                     @RequestParam(name = "sort", defaultValue = AppConstants.sortForWorkspaceMember) String by,
                                                                                     @RequestParam(name = "direction", defaultValue = AppConstants.direction) String direction) {

        Integer ownerId = getUserId(request);
        return ResponseEntity.ok(memberService.getMembers(workspaceId, ownerId, page, size, by, direction));
    }
}