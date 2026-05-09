package com.flowboard.board_service;

import com.flowboard.board_service.client.WorkspaceClient;
import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.dto.BoardUpdateRequestDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.entity.BoardMember;
import com.flowboard.board_service.entity.Visibility;
import com.flowboard.board_service.exception.IllegalOperationException;
import com.flowboard.board_service.mapper.impl.BoardRequestMapper;
import com.flowboard.board_service.mapper.impl.BoardResponseMapper;
import com.flowboard.board_service.repository.BoardMemberRepository;
import com.flowboard.board_service.repository.BoardRepository;
import com.flowboard.board_service.service.impl.BoardServiceImpl;
import org.springframework.data.domain.PageImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
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
    void createBoard_withDuplicateName_throwsException() {
        BoardRequestDto request = new BoardRequestDto();
        request.setWorkspaceId(10);
        request.setName("Board");

        when(workspaceClient.getOwnerId(10)).thenReturn(1);
        when(boardRepository.existsByNameAndWorkspaceId("Board", 10)).thenReturn(true);

        assertThrows(IllegalOperationException.class, () -> boardService.createBoard(request, 1));
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
    void getBoardById_withPrivateBoardAndMember_returnsDto() {
        Board board = new Board();
        board.setVisibility(Visibility.PRIVATE);
        BoardResponseDto dto = new BoardResponseDto();

        when(boardRepository.findById(1)).thenReturn(Optional.of(board));
        when(boardMemberRepository.existsByBoardIdAndUserId(1, 2)).thenReturn(true);
        when(boardResponseMapper.mapTo(board)).thenReturn(dto);

        BoardResponseDto result = boardService.getBoardById(1, 2);

        assertEquals(dto, result);
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
    void closeBoard_whenAlreadyClosed_throwsException() {
        Board board = new Board();
        board.setCreatedById(1);
        board.setClosed(true);
        when(boardRepository.findById(1)).thenReturn(Optional.of(board));

        assertThrows(IllegalOperationException.class, () -> boardService.closeBoard(1, 1));
    }

    @Test
    void openBoard_whenAlreadyOpen_throwsException() {
        Board board = new Board();
        board.setCreatedById(1);
        board.setClosed(false);
        when(boardRepository.findById(1)).thenReturn(Optional.of(board));

        assertThrows(IllegalOperationException.class, () -> boardService.openBoard(1, 1));
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

    @Test
    void getPublicBoardsForWorkspace_whenWorkspacePrivate_throwsException() {
        when(workspaceClient.isPrivate(10)).thenReturn(true);

        assertThrows(IllegalOperationException.class,
                () -> boardService.getPublicBoardsForWorkspace(10, 0, 10, "name", "asc"));
    }

    @Test
    void getPublicBoardsForWorkspace_whenWorkspacePublic_returnsPage() {
        Board board = new Board();
        BoardResponseDto dto = new BoardResponseDto();
        when(workspaceClient.isPrivate(10)).thenReturn(false);
        when(boardRepository.findByWorkspaceIdAndVisibility(eq(10), eq(Visibility.PUBLIC), any()))
                .thenReturn(new PageImpl<>(List.of(board)));
        when(boardResponseMapper.mapTo(board)).thenReturn(dto);

        var result = boardService.getPublicBoardsForWorkspace(10, 0, 10, "name", "desc");

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getPrivateBoardsByWorkspace_returnsPage() {
        Board board = new Board();
        BoardResponseDto dto = new BoardResponseDto();
        when(boardRepository.findPrivateBoardsByWorkspaceAndUser(eq(10), eq(2), any()))
                .thenReturn(new PageImpl<>(List.of(board)));
        when(boardResponseMapper.mapTo(board)).thenReturn(dto);

        var result = boardService.getPrivateBoardsByWorkspace(10, 2, 0, 10, "name", "asc");

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getPublicBoardsForLoggedUser_whenNotMember_throwsException() {
        when(workspaceClient.isMember(10, 2)).thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> boardService.getPublicBoardsForLoggedUser(10, 2, 0, 10, "name", "asc"));
    }

    @Test
    void getPublicBoardsForLoggedUser_whenMember_returnsPage() {
        Board board = new Board();
        BoardResponseDto dto = new BoardResponseDto();
        when(workspaceClient.isMember(10, 2)).thenReturn(true);
        when(boardRepository.findByWorkspaceIdAndVisibility(eq(10), eq(Visibility.PUBLIC), any()))
                .thenReturn(new PageImpl<>(List.of(board)));
        when(boardResponseMapper.mapTo(board)).thenReturn(dto);

        var result = boardService.getPublicBoardsForLoggedUser(10, 2, 0, 10, "name", "desc");

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getAllBoards_returnsPage() {
        Board board = new Board();
        BoardResponseDto dto = new BoardResponseDto();
        when(boardRepository.findAll(any(org.springframework.data.domain.Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(board)));
        when(boardResponseMapper.mapTo(board)).thenReturn(dto);

        var result = boardService.getAllBoards(0, 10, "name", "asc");

        assertEquals(1, result.getContent().size());
    }

    @Test
    void deleteBoardAsAdmin_deletesBoard() {
        Board board = new Board();
        when(boardRepository.findById(1)).thenReturn(Optional.of(board));

        boardService.deleteBoardAsAdmin(1);

        verify(boardRepository).delete(board);
    }
}
