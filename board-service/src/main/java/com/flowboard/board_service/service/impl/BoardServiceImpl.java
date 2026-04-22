package com.flowboard.board_service.service.impl;

import com.flowboard.board_service.client.WorkspaceClient;
import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.dto.BoardUpdateRequestDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.entity.BoardMember;
import com.flowboard.board_service.entity.Visibility;
import com.flowboard.board_service.exception.BoardNotFoundException;
import com.flowboard.board_service.exception.IllegalOperationException;
import com.flowboard.board_service.mapper.Mapper;
import com.flowboard.board_service.repository.BoardMemberRepository;
import com.flowboard.board_service.repository.BoardRepository;
import com.flowboard.board_service.service.BoardService;
import com.flowboard.board_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final Mapper<BoardRequestDto, Board> boardRequestMapper;
    private final Mapper<Board, BoardResponseDto> boardResponseMapper;
    private final WorkspaceClient workspaceClient;
    private final BoardMemberRepository boardMemberRepository;

    @Override
    public BoardResponseDto createBoard(BoardRequestDto boardRequestDto, Integer userId) {
        Integer workspaceId = boardRequestDto.getWorkspaceId();

        validateCreationAccess(workspaceId, userId);

        if(boardRepository.existsByNameAndWorkspaceId(boardRequestDto.getName(), workspaceId)) {
            throw new IllegalOperationException("Board already exist in workspace with same name");
        }

        Board board = boardRequestMapper.mapTo(boardRequestDto);
        board.setCreatedById(userId);

        Board savedBoard = boardRepository.save(board);

        BoardMember member = BoardMember
                .builder()
                .boardId(board.getBoardId())
                .userId(userId)
                .build();

        boardMemberRepository.save(member);

        return boardResponseMapper.mapTo(savedBoard);
    }

    /*
        here, optimization as board is only created by workspace owner so
        the board creator id is always same as workspace owner so to check weather the
        user with given userId(currently logged user) can update this board can be
        validated by just checking weather userId is same as board owner this way we
        not need to call the workspace service again and again and increases speed
    */
    @Transactional
    @Override
    public BoardResponseDto updateBoard(Integer boardId, BoardUpdateRequestDto dto, Integer userId) {
        Board savedBoard = getBoard(boardId);

        validateBoardModificationAccess(savedBoard, userId);

        savedBoard.setName(dto.getName());
        savedBoard.setDescription(dto.getDescription());
        savedBoard.setBackground(dto.getBackground());
        savedBoard.setVisibility(dto.getVisibility());

        Board updatedBoard = boardRepository.save(savedBoard);

        return boardResponseMapper.mapTo(updatedBoard);
    }

    @Override
    public void deleteBoard(Integer boardId, Integer userId) {
        Board board = getBoard(boardId);

        validateBoardModificationAccess(board, userId);

        boardRepository.delete(board);
    }

    @Override
    public CustomPageResponse<BoardResponseDto> getPublicBoardsForWorkspace(Integer workspaceId,
                                                                     Integer page,
                                                                     Integer size,
                                                                     String by,
                                                                     String direction) {

        if(workspaceClient.isPrivate(workspaceId)) {
            throw new IllegalOperationException("You cannot access this workspace");
        }

        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(by).ascending();
        else sort = Sort.by(by).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Board> boardPage = boardRepository.findByWorkspaceIdAndVisibility(workspaceId, Visibility.PUBLIC, pageable);

        Page<BoardResponseDto> boardResponseDtoPage = boardPage.map(boardResponseMapper::mapTo);

        return new CustomPageResponse<>(boardResponseDtoPage);
    }

    /*
    This return private board in which the user is a board member
    here we not check weather the user is member of workspace as if the usre in in boardmember
     table means the user is also in workspace this is enforeced there
     */
    @Override
    public CustomPageResponse<BoardResponseDto> getPrivateBoardsByWorkspace(Integer workspaceId,
                                                                            Integer userId,
                                                                            Integer page,
                                                                            Integer size,
                                                                            String sort,
                                                                            String direction) {
        Sort sorting = direction.equals("asc")
                ? Sort.by(sort).ascending()
                : Sort.by(sort).descending();

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Board> boardPage =
                boardRepository.findPrivateBoardsByWorkspaceAndUser(workspaceId, userId, pageable);

        Page<BoardResponseDto> dtoPage = boardPage.map(boardResponseMapper::mapTo);

        return new CustomPageResponse<>(dtoPage);
    }

    /*
    There are two different methods looking same get public boards one is for logged user
    and one is for guest so the logged user one will allow user to see if use is member
    and the other will ge for non logged user and will return the public board if the
    worksapce is public
     */
    @Override
    public CustomPageResponse<BoardResponseDto> getPublicBoardsForLoggedUser(Integer workspaceId,
                                                                             Integer userId,
                                                                             Integer page,
                                                                             Integer size,
                                                                             String by,
                                                                             String direction) {
        if(!workspaceClient.isMember(workspaceId, userId)) {
            throw new IllegalOperationException("You are not part of this workspace");
        }

        Sort sort = direction.equals("asc")
                ? Sort.by(by).ascending()
                : Sort.by(by).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Board> boardPage =
                boardRepository.findByWorkspaceIdAndVisibility(workspaceId, Visibility.PUBLIC, pageable);

        Page<BoardResponseDto> dtoPage =
                boardPage.map(boardResponseMapper::mapTo);

        return new CustomPageResponse<>(dtoPage);
    }

    @Override
    public BoardResponseDto getBoardById(Integer boardId, Integer userId) {
        Board board = getBoard(boardId);

        if(board.getVisibility().equals(Visibility.PRIVATE) && !isMember(userId, boardId)) {
            throw new IllegalOperationException("You cannot access a private board");
        }
        return boardResponseMapper.mapTo(board);
    }

    @Override
    public void closeBoard(Integer boardId, Integer userId) {
        Board board = getBoard(boardId);

        validateBoardModificationAccess(board, userId);

        if(board.isClosed()) {
            throw new IllegalOperationException("Board is already closed!");
        }
        board.setClosed(true);
        boardRepository.save(board);
    }

    @Override
    public void openBoard(Integer boardId, Integer userId) {
        Board board = getBoard(boardId);

        validateBoardModificationAccess(board, userId);

        if(!board.isClosed()) {
            throw new IllegalOperationException("Board is already open!");
        }
        board.setClosed(false);
        boardRepository.save(board);
    }

    private Board getBoard(Integer id) {
        return boardRepository.findById(id)
                .orElseThrow(() -> new BoardNotFoundException("Board not found with id " + id.toString()));
    }

    private boolean isMember(Integer userId, Integer boardId) {
        return boardMemberRepository.existsByBoardIdAndUserId(boardId, userId);
    }

    /*
        check only workspace owner can perform board level operations
     */
    private void validateCreationAccess(Integer workspaceId, Integer userId) {
        if(!workspaceClient.getOwnerId(workspaceId).equals(userId)) {
            throw new IllegalOperationException("You are not allowed to create board in this workspace");
        }
    }

    /*
    board createdById is same as workspace owner id as enforced when creating the board
    so no need to call workspace service again and again for validation this will work
    and is way faster as no inter service communication is required.
    */
    private void validateBoardModificationAccess(Board board, Integer userId) {
        if(!board.getCreatedById().equals(userId)) {
            throw new IllegalOperationException("You are not allowed to modify the board");
        }
    }
}