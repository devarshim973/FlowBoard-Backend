package com.flowboard.auth_service.controller;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.User;
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

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User Controller", description = "User management APIs")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Get user by email")
    @ApiResponse(responseCode = "200", description = "User fetched successfully")
    @GetMapping("/user-email/{email}")
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
    public ResponseEntity<String> handelDeleteById(@PathVariable Integer userId, @RequestHeader("X-User-Id") Integer loggedInUserId) {
        return ResponseEntity.ok().body(userService.deleteById(userId, loggedInUserId));
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
        This will be called by board service to get UserResponseDto from userId
    */
    @GetMapping("/bulk")
    public List<UserDto> getUserBulk(@RequestParam List<Integer> userIds) {
        return userService.getBulkUser(userIds);
    }

    @Operation(summary = "Get email of user by id",
            description = "Used by notification service to get mail of user to send notification email")
    @ApiResponse(responseCode = "200", description = "Returns email of user with given id")
    @GetMapping("/email/{id}")
    public ResponseEntity<String> getUserEmail(@PathVariable Integer id) {
        String email = userService.getEmailById(id);
        return ResponseEntity.ok(email);
    }

    @GetMapping("/findAll")
    public List<Integer> getUserIdsByUsername(@RequestParam List<String> userEmailList) {
        return userService.findAllUserIdByEmail(userEmailList);
    }

    @GetMapping("/check/{userId}")
    public Boolean checkUser(@PathVariable(value = "userId") Integer userId) {
        return userService.checkByUserId(userId);
    }
}
