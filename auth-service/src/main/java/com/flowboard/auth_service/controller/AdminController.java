package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.utils.AppConstants;
import com.flowboard.auth_service.utils.CustomPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "Admin controller", description = "Manage the user in the platform")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'PLATFORM_ADMIN')")
public class AdminController {
    private static final Logger adminActivityLogger = LoggerFactory.getLogger("ADMIN_ACTIVITY");
    private final UserService userService;

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        CustomPageResponse<UserDto> users = userService.findAll(0, 1, AppConstants.sortBy, AppConstants.direction);
        adminActivityLogger.info("Admin dashboard viewed");
        return ResponseEntity.ok(Map.of(
                "message", "Admin dashboard loaded",
                "totalUsers", users.getTotalNumberOfElements()
        ));
    }

    @Operation(summary = "Search users by full name")
    @GetMapping("/name/{fullName}")
    public ResponseEntity<CustomPageResponse<UserDto>> handlerGetUserByFullName(@PathVariable String fullName,
                                                                                @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                                @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                                @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                                @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        adminActivityLogger.info("Admin searched users by full name {}", fullName);
        return ResponseEntity.ok().body(userService.searchByFullName(fullName, page, size, sortBy, direction));
    }

    @Operation(summary = "Search user by email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> handlerGetUserByEmail(@PathVariable String email) {
        adminActivityLogger.info("Admin searched user by email {}", email);
        return ResponseEntity.ok().body(userService.searchByEmail(email));
    }

    @GetMapping({"/user/all", "/users"})
    public ResponseEntity<CustomPageResponse<UserDto>> handleGetAllUser(@RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                        @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                        @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                        @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        adminActivityLogger.info("Admin requested user list page {} size {}", page, size);
        return ResponseEntity.ok().body(userService.findAll(page, size, sortBy, direction));
    }

    @DeleteMapping({"/{userId}", "/user/{userId}"})
    public ResponseEntity<String> handleDeleteUser(@PathVariable Integer userId) {
        adminActivityLogger.info("Admin deleting user {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @PutMapping("/disable/{userId}")
    public ResponseEntity<String> handleDisableUser(@PathVariable Integer userId) {
        adminActivityLogger.info("Admin disabling user {}", userId);
        userService.disable(userId);
        return ResponseEntity.ok().body("User disabled successfully");
    }

    @PutMapping("/enable/{userId}")
    public ResponseEntity<String> handleEnableUser(@PathVariable Integer userId) {
        adminActivityLogger.info("Admin enabling user {}", userId);
        userService.enable(userId);
        return ResponseEntity.ok().body("User enabled successfully");
    }

    @PutMapping("/user/{userId}/status")
    public ResponseEntity<String> handleUserStatusUpdate(@PathVariable Integer userId,
                                                         @RequestParam boolean active) {
        adminActivityLogger.info("Admin updated user {} status to {}", userId, active);
        if (active) {
            userService.enable(userId);
            return ResponseEntity.ok("User enabled successfully");
        }
        userService.disable(userId);
        return ResponseEntity.ok("User disabled successfully");
    }
}
