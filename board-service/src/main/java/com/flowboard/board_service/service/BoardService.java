package com.flowboard.board_service.service;

import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.dto.BoardUpdateRequestDto;
import com.flowboard.board_service.util.CustomPageResponse;

import java.util.List;

public interface BoardService {
    public BoardResponseDto createBoard(BoardRequestDto dto, Integer userId);

    public BoardResponseDto updateBoard(Integer boardId, BoardUpdateRequestDto dto, Integer userId);

    public void deleteBoard(Integer boardId, Integer userId);

    public CustomPageResponse<BoardResponseDto> getPrivateBoardsByWorkspace(
            Integer workspaceId,
            Integer userId,
            Integer page,
            Integer size,
            String sort,
            String direction
    );

    public CustomPageResponse<BoardResponseDto> getPublicBoardsForWorkspace(
            Integer workspaceId,
            Integer page,
            Integer size,
            String sort,
            String direction
    );

    public CustomPageResponse<BoardResponseDto> getPublicBoardsForLoggedUser(Integer workspaceId,
                                                                             Integer userId,
                                                                             Integer page,
                                                                             Integer size,
                                                                             String sort,
                                                                             String direction);

    public BoardResponseDto getBoardById(Integer boardId, Integer userId);

    public void closeBoard(Integer boardId, Integer userId);

    public void openBoard(Integer boardId, Integer userId);

    // public CustomPageResponse<BoardResponseDto> getPublicBoardsForWorkspace(Integer workspaceId, Integer userId);
}