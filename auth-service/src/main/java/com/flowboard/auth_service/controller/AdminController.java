package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.utils.AppConstants;
import com.flowboard.auth_service.utils.CustomPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin controller", description = "Manage the user in the platform")
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("{hasRole('ADMIN')}")
public class AdminController {
    private final UserService userService;
    @Operation(summary = "Search users by full name")
    @GetMapping("/name/{fullName}")
    public ResponseEntity<CustomPageResponse<UserDto>> handlerGetUserByFullName(@PathVariable String fullName,
                                                                                @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                                @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                                @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                                @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        return ResponseEntity.ok().body(userService.searchByFullName(fullName, page, size, sortBy, direction));
    }

    @Operation(summary = "Search user by email")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> handlerGetUserByEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(userService.searchByEmail(email));
    }

    @GetMapping("/user/all")
    public ResponseEntity<CustomPageResponse<UserDto>>  handleGetAllUser(@RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                         @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                         @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                         @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        return ResponseEntity.ok().body(userService.findAll(page, size, sortBy, direction));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> handleDeleteUser(@PathVariable Integer userId) {
        userService.deleteUser(userId);
        return ResponseEntity.ok().body("User deleted successfully");
    }

    @PutMapping("/disable/{userId}")
    public ResponseEntity<String> handleDisableUser(@PathVariable Integer userId) {
        userService.disable(userId);
        return ResponseEntity.ok().body("User disabled successfully");
    }

    @PutMapping("/enable/{userId}")
    public ResponseEntity<String> handleEnableUser(@PathVariable Integer userId) {
        userService.enable(userId);
        return ResponseEntity.ok().body("User enabled successfully");
    }
}
