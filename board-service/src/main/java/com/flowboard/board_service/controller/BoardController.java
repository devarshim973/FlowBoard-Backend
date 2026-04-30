package com.flowboard.board_service.controller;

import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.dto.BoardUpdateRequestDto;
import com.flowboard.board_service.service.BoardService;
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
@RequestMapping("/api/v1/boards")
@RequiredArgsConstructor
@Tag(name = "Board Controller", description = "Board management related APIs")
public class BoardController {
    private final BoardService boardService;

    private Integer getUserId(HttpServletRequest request) {
        return Integer.parseInt(request.getHeader("X-User-Id"));
    }

    @Operation(summary = "Create board", description = "Creates a new board inside workspace")
    @ApiResponse(responseCode = "200", description = "Board created successfully")
    @PostMapping("/create")
    public ResponseEntity<BoardResponseDto> createBoard(@RequestBody BoardRequestDto dto, HttpServletRequest request) {
        Integer userId = getUserId(request);

        return ResponseEntity.ok(boardService.createBoard(dto, userId));
    }

    @Operation(summary = "Update board", description = "Updates board details")
    @ApiResponse(responseCode = "201", description = "Board updated successfully")
    @PutMapping("/update/{boardId}")
    public ResponseEntity<BoardResponseDto> updateBoard(@PathVariable Integer boardId,
                                                        @RequestBody BoardUpdateRequestDto dto,
                                                        HttpServletRequest request) {
        Integer userId = getUserId(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(boardService.updateBoard(boardId, dto, userId));
    }

    @Operation(summary = "Delete board", description = "Deletes board by ID")
    @ApiResponse(responseCode = "202", description = "Board deleted successfully")
    @DeleteMapping("/delete/{boardId}")
    public ResponseEntity<String> deleteBoard(@PathVariable Integer boardId, HttpServletRequest request) {
        Integer userId = getUserId(request);

        boardService.deleteBoard(boardId, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Board deleted successfully");
    }

    @Operation(summary = "Get board by ID", description = "Returns board details by board ID")
    @ApiResponse(responseCode = "200", description = "Board fetched successfully")
    @GetMapping("/get/{boardId}")
    public ResponseEntity<BoardResponseDto> getBoardById(@PathVariable Integer boardId, HttpServletRequest request) {
        Integer userId = Integer.parseInt(request.getHeader("X-User-Id"));
        return ResponseEntity.ok(boardService.getBoardById(boardId, userId));
    }

    /*
    Return public board for public workspace
    use this if a logged user is looking for public board of public workspace
     */
    @Operation(summary = "Get public boards", description = "Returns public boards of a public workspace")
    @ApiResponse(responseCode = "200", description = "Boards fetched successfully")
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
    @Operation(summary = "Get member public boards", description = "Returns public boards of private workspace if user is member")
    @ApiResponse(responseCode = "200", description = "Boards fetched successfully")
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

    @Operation(summary = "Get private boards", description = "Returns private boards of workspace for authorized user")
    @ApiResponse(responseCode = "200", description = "Boards fetched successfully")
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

    @Operation(summary = "Close board", description = "Marks board as closed")
    @ApiResponse(responseCode = "202", description = "Board closed successfully")
    @PutMapping("/{boardId}/close")
    public ResponseEntity<String> closeBoard(@PathVariable Integer boardId, HttpServletRequest request) {
        Integer userId = getUserId(request);

        boardService.closeBoard(boardId, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Board closed successfully");
    }

    @Operation(summary = "Open board", description = "Reopens closed board")
    @ApiResponse(responseCode = "202", description = "Board opened successfully")
    @PutMapping("/{boardId}/open")
    public ResponseEntity<String> openBoard(@PathVariable Integer boardId, HttpServletRequest request) {
        Integer userId = getUserId(request);

        boardService.openBoard(boardId, userId);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body("Board opened successfully");
    }

    @Operation(summary = "Get workspace ID by board", description = "Returns workspace ID of given board")
    @ApiResponse(responseCode = "200", description = "Workspace ID fetched successfully")
    @GetMapping("/workspace/{boardId}")
    public Integer getWorkspaceId(@PathVariable(value = "boardId") Integer boardId) {
        return boardService.getWorkspaceId(boardId);
    }

    @Operation(summary = "Check board privacy", description = "Returns true if board is private")
    @ApiResponse(responseCode = "200", description = "Privacy status fetched successfully")
    @GetMapping("/is-private/{boardId}")
    public Boolean isPrivate(@PathVariable(value = "boardId") Integer boardId) {
        return boardService.isPrivate(boardId);
    }
}