package com.flowboard.board_service.controller;

import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.service.BoardService;
import com.flowboard.board_service.util.AppConstants;
import com.flowboard.board_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/boards")
@RequiredArgsConstructor
public class AdminBoardController {
    private final BoardService boardService;

    @GetMapping
    public ResponseEntity<CustomPageResponse<BoardResponseDto>> getAllBoards(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(defaultValue = AppConstants.page) Integer page,
            @RequestParam(defaultValue = AppConstants.size) Integer size,
            @RequestParam(defaultValue = AppConstants.sortBoard) String by,
            @RequestParam(defaultValue = AppConstants.direction) String direction) {
        ensureAdmin(role);
        return ResponseEntity.ok(boardService.getAllBoards(page, size, by, direction));
    }

    @DeleteMapping("/{boardId}")
    public ResponseEntity<String> deleteBoard(@RequestHeader("X-User-Role") String role,
                                              @PathVariable Integer boardId) {
        ensureAdmin(role);
        boardService.deleteBoardAsAdmin(boardId);
        return ResponseEntity.ok("Board deleted successfully");
    }

    private void ensureAdmin(String role) {
        if (!"PLATFORM_ADMIN".equals(role)) {
            throw new IllegalArgumentException("Admin access required");
        }
    }
}
