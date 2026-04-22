package com.flowboard.board_service.controller;

import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.dto.UserDto;
import com.flowboard.board_service.service.BoardMemberService;
import com.flowboard.board_service.util.AppConstants;
import com.flowboard.board_service.util.CustomPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/board-members")
@RequiredArgsConstructor
public class BoardMemberController {
    private final BoardMemberService boardMemberService;

    private Integer getUserId(HttpServletRequest request) {
        return Integer.parseInt(request.getHeader("X-User-Id"));
    }

    @PostMapping("/add")
    public ResponseEntity<BoardMemberResponseDto> addMember(@RequestBody BoardMemberRequestDto dto, HttpServletRequest request) {
        Integer userId = getUserId(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(boardMemberService.addMember(dto, userId));
    }

    @DeleteMapping("/remove/{boardId}/{memberUserId}")
    public ResponseEntity<String> removeMember(@PathVariable Integer boardId, @PathVariable Integer memberUserId, HttpServletRequest request) {
        Integer userId = getUserId(request);

        boardMemberService.removeMember(boardId, memberUserId, userId);

        return ResponseEntity.status(HttpStatus.CREATED).body("Member removed successfully");
    }

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
}