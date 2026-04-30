package com.flowboard.board_service;

import com.flowboard.board_service.client.WorkspaceClient;
import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.dto.BoardUpdateRequestDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.entity.Visibility;
import com.flowboard.board_service.exception.IllegalOperationException;
import com.flowboard.board_service.mapper.impl.BoardRequestMapper;
import com.flowboard.board_service.mapper.impl.BoardResponseMapper;
import com.flowboard.board_service.repository.BoardMemberRepository;
import com.flowboard.board_service.repository.BoardRepository;
import com.flowboard.board_service.service.impl.BoardServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BoardServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardRequestMapper boardRequestMapper;

    @Mock
    private BoardResponseMapper boardResponseMapper;

    @Mock
    private WorkspaceClient workspaceClient;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @InjectMocks
    private BoardServiceImpl boardService;

    @Test
    void createBoard_withValidData_returnsDto() {

        BoardRequestDto request = new BoardRequestDto();
        request.setWorkspaceId(10);
        request.setName("Board");

        Board board = new Board();
        board.setBoardId(1);

        BoardResponseDto dto = new BoardResponseDto();
        dto.setBoardId(1);

        when(workspaceClient.getOwnerId(10))
                .thenReturn(1);

        when(boardRepository.existsByNameAndWorkspaceId("Board", 10))
                .thenReturn(false);

        lenient().when(boardRequestMapper.mapTo(any()))
                .thenReturn(board);

        when(boardRepository.save(board))
                .thenReturn(board);

        lenient().when(boardResponseMapper.mapTo(any()))
                .thenReturn(dto);

        BoardResponseDto result =
                boardService.createBoard(request, 1);

        assertEquals(1, result.getBoardId());
    }

    @Test
    void createBoard_withWrongOwner_throwsException() {

        BoardRequestDto request = new BoardRequestDto();
        request.setWorkspaceId(10);

        when(workspaceClient.getOwnerId(10))
                .thenReturn(5);

        assertThrows(IllegalOperationException.class,
                () -> boardService.createBoard(request, 1));
    }

    @Test
    void updateBoard_withValidData_returnsDto() {

        Board board = new Board();
        board.setBoardId(1);
        board.setCreatedById(1);

        BoardUpdateRequestDto request =
                new BoardUpdateRequestDto();

        BoardResponseDto dto =
                new BoardResponseDto();
        dto.setBoardId(1);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        when(boardRepository.save(board))
                .thenReturn(board);

        when(boardResponseMapper.mapTo(board))
                .thenReturn(dto);

        BoardResponseDto result =
                boardService.updateBoard(1, request, 1);

        assertEquals(1, result.getBoardId());
    }

    @Test
    void updateBoard_withWrongUser_throwsException() {

        Board board = new Board();
        board.setCreatedById(5);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        assertThrows(IllegalOperationException.class,
                () -> boardService.updateBoard(
                        1,
                        new BoardUpdateRequestDto(),
                        1
                ));
    }

    @Test
    void deleteBoard_withValidUser_deletesBoard() {

        Board board = new Board();
        board.setCreatedById(1);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        boardService.deleteBoard(1, 1);

        verify(boardRepository).delete(board);
    }

    @Test
    void deleteBoard_withWrongUser_throwsException() {

        Board board = new Board();
        board.setCreatedById(5);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        assertThrows(IllegalOperationException.class,
                () -> boardService.deleteBoard(1, 1));
    }

    @Test
    void getBoardById_withPublicBoard_returnsDto() {

        Board board = new Board();
        board.setVisibility(Visibility.PUBLIC);

        BoardResponseDto dto =
                new BoardResponseDto();

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        when(boardResponseMapper.mapTo(board))
                .thenReturn(dto);

        BoardResponseDto result =
                boardService.getBoardById(1, 2);

        assertEquals(dto, result);
    }

    @Test
    void getBoardById_withPrivateBoardAndNonMember_throwsException() {

        Board board = new Board();
        board.setVisibility(Visibility.PRIVATE);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        when(boardMemberRepository.existsByBoardIdAndUserId(1, 2))
                .thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> boardService.getBoardById(1, 2));
    }

    @Test
    void closeBoard_withValidBoard_updatesStatus() {

        Board board = new Board();
        board.setCreatedById(1);
        board.setClosed(false);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        boardService.closeBoard(1, 1);

        verify(boardRepository).save(board);
    }

    @Test
    void openBoard_withValidBoard_updatesStatus() {

        Board board = new Board();
        board.setCreatedById(1);
        board.setClosed(true);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        boardService.openBoard(1, 1);

        verify(boardRepository).save(board);
    }

    @Test
    void getWorkspaceId_withValidId_returnsId() {

        Board board = new Board();
        board.setWorkspaceId(10);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        Integer result =
                boardService.getWorkspaceId(1);

        assertEquals(10, result);
    }

    @Test
    void isPrivate_withPrivateBoard_returnsTrue() {

        Board board = new Board();
        board.setVisibility(Visibility.PRIVATE);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        Boolean result =
                boardService.isPrivate(1);

        assertEquals(true, result);
    }
}