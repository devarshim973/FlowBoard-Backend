package com.flowboard.workspace_service.controller;

import com.flowboard.workspace_service.dto.WorkspaceMemberRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceMemberResponseDto;
import com.flowboard.workspace_service.service.WorkspaceMemberService;
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
public class WorkspaceMemberController {

    private final WorkspaceMemberService memberService;

    private Integer getUserId(HttpServletRequest request) {
        return Integer.parseInt(request.getHeader("X-User-Id"));
    }

    @PostMapping("/add")
    public ResponseEntity<WorkspaceMemberResponseDto> handleAddMember(@RequestBody @Valid WorkspaceMemberRequestDto workspaceMemberRequestDto,
                                                                HttpServletRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED).body(memberService.addMember(workspaceMemberRequestDto, getUserId(request)));
    }

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