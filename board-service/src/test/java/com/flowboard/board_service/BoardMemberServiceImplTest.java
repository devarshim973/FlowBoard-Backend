package com.flowboard.board_service;

import com.flowboard.board_service.client.UserClient;
import com.flowboard.board_service.client.WorkspaceClient;
import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.dto.UserDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.entity.BoardMember;
import com.flowboard.board_service.exception.IllegalOperationException;
import com.flowboard.board_service.mapper.impl.BoardMemberRequestMapper;
import com.flowboard.board_service.mapper.impl.BoardMemberResponseMapper;
import com.flowboard.board_service.repository.BoardMemberRepository;
import com.flowboard.board_service.repository.BoardRepository;
import com.flowboard.board_service.service.impl.BoardMemberServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class BoardMemberServiceImplTest {

    @Mock
    private BoardRepository boardRepository;

    @Mock
    private BoardMemberRepository boardMemberRepository;

    @Mock
    private BoardMemberRequestMapper boardMemberRequestMapper;

    @Mock
    private BoardMemberResponseMapper boardMemberResponseMapper;

    @Mock
    private WorkspaceClient workspaceClient;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private BoardMemberServiceImpl boardMemberService;

    @Test
    void addMember_withValidData_returnsResponse() {

        Board board = new Board();
        board.setBoardId(1);
        board.setCreatedById(1);
        board.setWorkspaceId(10);

        BoardMemberRequestDto request =
                new BoardMemberRequestDto();
        request.setBoardId(1);
        request.setUserId(2);

        BoardMember boardMember =
                new BoardMember();

        BoardMemberResponseDto response =
                new BoardMemberResponseDto();
        response.setBoardId(1);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        when(workspaceClient.isMember(10, 2))
                .thenReturn(true);

        when(boardMemberRepository.existsByBoardIdAndUserId(1, 2))
                .thenReturn(false);

        lenient().when(boardMemberRequestMapper.mapTo(any()))
                .thenReturn(boardMember);

        when(boardMemberRepository.save(boardMember))
                .thenReturn(boardMember);

        lenient().when(boardMemberResponseMapper.mapTo(any()))
                .thenReturn(response);

        BoardMemberResponseDto result =
                boardMemberService.addMember(request, 1);

        assertEquals(1, result.getBoardId());
    }

    @Test
    void addMember_withWrongOwner_throwsException() {

        Board board = new Board();
        board.setCreatedById(5);

        BoardMemberRequestDto request =
                new BoardMemberRequestDto();
        request.setBoardId(1);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        assertThrows(IllegalOperationException.class,
                () -> boardMemberService.addMember(request, 1));
    }

    @Test
    void removeMember_withValidIds_deletesMember() {

        Board board = new Board();
        board.setCreatedById(1);

        BoardMember member =
                new BoardMember();

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        when(boardMemberRepository.findByBoardIdAndUserId(1, 2))
                .thenReturn(Optional.of(member));

        boardMemberService.removeMember(1, 2, 1);

        verify(boardMemberRepository).delete(member);
    }

    @Test
    void removeMember_withWrongOwner_throwsException() {

        Board board = new Board();
        board.setCreatedById(5);

        when(boardRepository.findById(1))
                .thenReturn(Optional.of(board));

        assertThrows(IllegalOperationException.class,
                () -> boardMemberService.removeMember(1, 2, 1));
    }

    @Test
    void getMembers_withValidData_returnsPage() {

        when(boardMemberRepository.existsByBoardIdAndUserId(1, 2))
                .thenReturn(true);

        when(boardMemberRepository.findUserIdsByBoardId(any(), any()))
                .thenReturn(new PageImpl<>(
                        List.of(2),
                        PageRequest.of(0, 5),
                        1
                ));

        when(userClient.getUserBulk(List.of(2)))
                .thenReturn(List.of(new UserDto()));

        var result =
                boardMemberService.getMembers(
                        1, 2, 0, 5, "id", "asc"
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getMembers_withNonMember_throwsException() {

        when(boardMemberRepository.existsByBoardIdAndUserId(1, 2))
                .thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> boardMemberService.getMembers(
                        1, 2, 0, 5, "id", "asc"
                ));
    }

    @Test
    void checkIsMember_withValidData_returnsTrue() {

        when(boardMemberRepository.existsByBoardIdAndUserId(1, 2))
                .thenReturn(true);

        Boolean result =
                boardMemberService.checkIsMember(1, 2);

        assertEquals(true, result);
    }

    @Test
    void checkIsMember_withWrongData_returnsFalse() {

        when(boardMemberRepository.existsByBoardIdAndUserId(1, 2))
                .thenReturn(false);

        Boolean result =
                boardMemberService.checkIsMember(1, 2);

        assertEquals(false, result);
    }
}