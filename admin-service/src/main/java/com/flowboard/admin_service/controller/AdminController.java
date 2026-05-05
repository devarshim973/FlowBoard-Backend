package com.flowboard.admin_service.controller;

import com.flowboard.admin_service.dto.LoginDto;
import com.flowboard.admin_service.dto.UserDto;
import com.flowboard.admin_service.service.AdminService;
import com.flowboard.admin_service.util.CustomPageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminController {
    private final AdminService adminService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginDto loginDto) {
        return ResponseEntity.ok(adminService.login(loginDto));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(adminService.dashboard());
    }

    @GetMapping({"/users", "/user/all"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<CustomPageResponse<UserDto>> getUsers(
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.findAllUsers(page, size, sortBy, direction));
    }

    @GetMapping("/name/{fullName}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<CustomPageResponse<UserDto>> getByFullName(
            @PathVariable String fullName,
            @RequestParam(value = "sortBy", defaultValue = "createdAt") String sortBy,
            @RequestParam(value = "direction", defaultValue = "desc") String direction,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        return ResponseEntity.ok(adminService.searchByFullName(fullName, page, size, sortBy, direction));
    }

    @GetMapping("/email/{email}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<UserDto> getByEmail(@PathVariable String email) {
        return ResponseEntity.ok(adminService.searchByEmail(email));
    }

    @PutMapping("/user/{userId}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<String> updateUserStatus(@PathVariable Integer userId, @RequestParam boolean active) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, active));
    }

    @PutMapping("/disable/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<String> disableUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, false));
    }

    @PutMapping("/enable/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<String> enableUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.updateUserStatus(userId, true));
    }

    @DeleteMapping({"/{userId}", "/user/{userId}"})
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<String> deleteUser(@PathVariable Integer userId) {
        return ResponseEntity.ok(adminService.deleteUser(userId));
    }

    @GetMapping("/workspaces")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Object> getWorkspaces() {
        return ResponseEntity.ok(adminService.getWorkspaces());
    }

    @DeleteMapping("/workspaces/{workspaceId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<String> deleteWorkspace(@PathVariable Integer workspaceId) {
        return ResponseEntity.ok(adminService.deleteWorkspace(workspaceId));
    }

    @GetMapping("/boards")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<Object> getBoards() {
        return ResponseEntity.ok(adminService.getBoards());
    }

    @DeleteMapping("/boards/{boardId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
    public ResponseEntity<String> deleteBoard(@PathVariable Integer boardId) {
        return ResponseEntity.ok(adminService.deleteBoard(boardId));
    }
}
