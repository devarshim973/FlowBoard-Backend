package com.flowboard.board_service.controller;

import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.dto.BoardUpdateRequestDto;
import com.flowboard.board_service.service.BoardService;
import com.flowboard.board_service.util.AppConstants;
import com.flowboard.board_service.util.CustomPageResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    private Integer getUserId(HttpServletRequest request) {
        return Integer.parseInt(request.getHeader("X-User-Id"));
    }

    @PostMapping("/create")
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto dto, HttpServletRequest request) {
        Integer userId = getUserId(request);

        return ResponseEntity.ok(boardService.createBoard(dto, userId));
    }

    @PutMapping("/update/{boardId}")
    public ResponseEntity<BoardResponseDto> updateBoard(@PathVariable Integer boardId,
                                                        @RequestBody BoardUpdateRequestDto dto,
                                                        HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.updateBoard(boardId, dto, userId));
    }

    @DeleteMapping("/delete/{boardId}")
    public ResponseEntity<String> deleteBoard(@PathVariable Integer boardId, HttpServletRequest request) {
        Integer userId = getUserId(request);

        boardService.deleteBoard(boardId, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Board deleted successfully");
    }

    @GetMapping("/get/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoardById(@PathVariable Integer boardId, HttpServletRequest request) {
        Integer userId = Integer.parseInt(request.getHeader("X-User-Id"));
        return ResponseEntity.ok(boardService.getBoardById(boardId, userId));
    }

    /*
    Return public board for public workspace
    use this if a logged user is looking for public board of public workspace
     */
    @GetMapping("/get/workspace/{workspaceId}/public")
    public ResponseEntity<CustomPageResponse<BoardResponseDto>> getPublicBoards(
            @PathVariable Integer workspaceId,
            @RequestParam(defaultValue = AppConstants.page) Integer page,
            @RequestParam(defaultValue = AppConstants.size) Integer size,
            @RequestParam(defaultValue = AppConstants.sortBoard) String by,
            @RequestParam(defaultValue = AppConstants.direction) String direction) {

        return ResponseEntity.ok(boardService.getPublicBoardsForWorkspace(workspaceId, page, size, by, direction));
    }

    /*
    Return public board of private workspace(if user is member)
     */
    @GetMapping("/workspace/{workspaceId}/member/public")
    public ResponseEntity<CustomPageResponse<BoardResponseDto>> getPublicBoardsForUser(
            @PathVariable Integer workspaceId,
            HttpServletRequest request,
            @RequestParam(defaultValue = AppConstants.page) Integer page,
            @RequestParam(defaultValue = AppConstants.size) Integer size,
            @RequestParam(defaultValue = AppConstants.sortBoard) String by,
            @RequestParam(defaultValue = AppConstants.direction) String direction) {

        Integer userId = getUserId(request);
        return ResponseEntity.ok(boardService.getPublicBoardsForLoggedUser(workspaceId, userId, page, size, by, direction));
    }

    @GetMapping("/workspace/{workspaceId}/private")
    public ResponseEntity<CustomPageResponse<BoardResponseDto>> getPrivateBoards(
            @PathVariable Integer workspaceId,
            HttpServletRequest request,
            @RequestParam(defaultValue = AppConstants.page) Integer page,
            @RequestParam(defaultValue = AppConstants.size) Integer size,
            @RequestParam(defaultValue = AppConstants.sortBoard) String by,
            @RequestParam(defaultValue = AppConstants.direction) String direction) {

        Integer userId = getUserId(request);
        return ResponseEntity.ok(boardService.getPrivateBoardsByWorkspace(workspaceId, userId, page, size, by, direction));
    }

    @PutMapping("/{boardId}/close")
    public ResponseEntity<String> closeBoard(@PathVariable Integer boardId, HttpServletRequest request) {
        Integer userId = getUserId(request);

        boardService.closeBoard(boardId, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Board closed successfully");
    }

    @PutMapping("/{boardId}/open")
    public ResponseEntity<String> openBoard(@PathVariable Integer boardId, HttpServletRequest request) {
        Integer userId = getUserId(request);

        boardService.openBoard(boardId, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Board opened successfully");
    }
}