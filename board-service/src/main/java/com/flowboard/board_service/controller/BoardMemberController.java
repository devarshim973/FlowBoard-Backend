package com.flowboard.board_service.controller;

import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.dto.UserDto;
import com.flowboard.board_service.service.BoardMemberService;
import com.flowboard.board_service.util.AppConstants;
import com.flowboard.board_service.util.CustomPageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/board-members")
@RequiredArgsConstructor
@Tag(name = "Board Member Controller", description = "Board member management APIs")
public class BoardMemberController {
    private final BoardMemberService boardMemberService;


    private Integer getUserId(HttpServletRequest request) {
        return Integer.parseInt(request.getHeader("X-User-Id"));
    }

    @Operation(summary = "Add member to board", description = "Adds a user as member to a board")
    @ApiResponse(responseCode = "201", description = "Member added successfully")
    @PostMapping("/add")
    public ResponseEntity<BoardMemberResponseDto> addMember(@RequestBody BoardMemberRequestDto dto, HttpServletRequest request) {
        Integer userId = getUserId(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(boardMemberService.addMember(dto, userId));
    }

    @Operation(summary = "Remove member from board", description = "Removes a member from board")
    @ApiResponse(responseCode = "201", description = "Member removed successfully")
    @DeleteMapping("/remove/{boardId}/{memberUserId}")
    public ResponseEntity<String> removeMember(@PathVariable Integer boardId, @PathVariable Integer memberUserId, HttpServletRequest request) {
        Integer userId = getUserId(request);

        boardMemberService.removeMember(boardId, memberUserId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body("Member removed successfully");
    }

    @Operation(summary = "Get board members", description = "Returns paginated list of board members")
    @ApiResponse(responseCode = "200", description = "Members fetched successfully")
    @GetMapping("/get/{boardId}")
    public ResponseEntity<CustomPageResponse<UserDto>> getMembers(
            @PathVariable Integer boardId,
            HttpServletRequest request,
            @RequestParam(defaultValue = AppConstants.page) Integer page,
            @RequestParam(defaultValue = AppConstants.size) Integer size,
            @RequestParam(defaultValue = AppConstants.sortMember) String by,
            @RequestParam(defaultValue = AppConstants.direction) String direction) {

        Integer userId = getUserId(request);

        return ResponseEntity.ok(boardMemberService.getMembers(boardId, userId, page, size, by, direction));
    }

    /*
    For inter-service communication and will be used by list service
     */
    @Operation(summary = "Check board membership", description = "Checks whether given user is member of board")
    @ApiResponse(responseCode = "200", description = "Membership status returned")
    @GetMapping("/{boardId}/is-member/{userId}")
    public Boolean isMember(
            @PathVariable(value = "boardId") Integer boardId,
            @PathVariable(value = "userId") Integer userId) {
        return boardMemberService.checkIsMember(boardId, userId);
    }
}