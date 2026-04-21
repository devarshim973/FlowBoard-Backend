package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.utils.AppConstants;
import com.flowboard.auth_service.utils.CustomPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User Controller", description = "User management APIs")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get user by email")
    @ApiResponse(responseCode = "200", description = "User fetched successfully")
    @GetMapping("/email/{email}")
    public ResponseEntity<UserDto> handelFindByEmail(@PathVariable String email) {
        return ResponseEntity.ok().body(userService.getUserByEmail(email));
    }

    @Operation(summary = "Get user by ID")
    @ApiResponse(responseCode = "200", description = "User fetched successfully")
    @GetMapping("/id/{userId}")
    public ResponseEntity<UserDto> handelFindById(@PathVariable Integer userId) {
        return ResponseEntity.ok().body(userService.getUserById(userId));
    }

    @Operation(summary = "Delete user")
    @ApiResponse(responseCode = "200", description = "User deleted successfully")
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity<String> handelDeleteById(@PathVariable Integer userId) {
        return ResponseEntity.ok().body(userService.deleteById(userId));
    }

    @Operation(summary = "Full user update")
    @ApiResponse(responseCode = "202", description = "User updated successfully")
    @PutMapping("/update/{id}")
    public ResponseEntity<UserDto> handelFullUpdate(@Valid @RequestBody UserUpdateDto userUpdateDto,
                                                    @PathVariable Integer id) {
        return ResponseEntity.accepted().body(userService.updateProfile(id, userUpdateDto));
    }

    @Operation(summary = "Update user avatar url")
    @ApiResponse(responseCode = "202", description = "User avatar url successfully")
    @PatchMapping("/avtarurl/{id}/{url}")
    public ResponseEntity<UserDto> handelUpdateAvatarUrl(@PathVariable Integer id,
                                                         @PathVariable String url) {
        return ResponseEntity.accepted().body(userService.updateAvatarUrl(id, url));
    }

    /*
    this should be only visible to specific roles
     */
    @Operation(summary = "Get users by role with pagination")
    @GetMapping("/role/{role}")
    public ResponseEntity<CustomPageResponse<UserDto>> handleGetUserByRole(@PathVariable String role,
                                                                           @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                           @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                           @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                           @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        return ResponseEntity.ok().body(userService.findAllByRole(role, page, size, sortBy, direction));
    }

    /*
        This should be only visible to specific roles
     */
    @Operation(summary = "Search users by full name")
    @GetMapping("/name/{fullName}")
    public ResponseEntity<CustomPageResponse<UserDto>> handlerGetUserByFullName(@PathVariable String fullName,
                                                                                @RequestParam(value = "sortBy", defaultValue = AppConstants.sortBy) String sortBy,
                                                                                @RequestParam(value = "direction", defaultValue = AppConstants.direction) String direction,
                                                                                @RequestParam(value = "page", defaultValue = AppConstants.page) int page,
                                                                                @RequestParam(value = "size", defaultValue = AppConstants.size) int size) {
        return ResponseEntity.ok().body(userService.searchByFullName(fullName, page, size, sortBy, direction));
    }
}
