package com.flowboard.board_service.service.impl;

import com.flowboard.board_service.client.UserClient;
import com.flowboard.board_service.client.WorkspaceClient;
import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.dto.UserDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.entity.BoardMember;
import com.flowboard.board_service.entity.BoardRole;
import com.flowboard.board_service.exception.BoardMemberNotFoundException;
import com.flowboard.board_service.exception.BoardNotFoundException;
import com.flowboard.board_service.exception.IllegalOperationException;
import com.flowboard.board_service.mapper.Mapper;
import com.flowboard.board_service.repository.BoardMemberRepository;
import com.flowboard.board_service.repository.BoardRepository;
import com.flowboard.board_service.service.BoardMemberService;
import com.flowboard.board_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BoardMemberServiceImpl implements BoardMemberService {
    private final BoardRepository boardRepository;
    private final BoardMemberRepository boardMemberRepository;
    private final Mapper<BoardMemberRequestDto, BoardMember> boardMemberRequestMapper;
    private final Mapper<BoardMember, BoardMemberResponseDto> boardMemberResponseMapper;
    private final WorkspaceClient workspaceClient;
    private final UserClient userClient;

    @Override
    public BoardMemberResponseDto addMember(BoardMemberRequestDto boardMemberRequestDto, Integer userId) {
        Integer boardId = boardMemberRequestDto.getBoardId();
        Integer memberId = boardMemberRequestDto.getUserId();

        Board board = getBoard(boardId);

        if(!board.getCreatedById().equals(userId)) {
            throw new IllegalOperationException("You cannot add member in this board");
        }

        if(!workspaceClient.isMember(board.getWorkspaceId(), memberId)) {
            throw new IllegalOperationException("The you are trying to add in board must be part of workspace");
        }

        if(boardMemberRepository.existsByBoardIdAndUserId(boardId, memberId)) {
            throw new IllegalOperationException("User already member of board");
        }

        BoardMember boardMember = boardMemberRequestMapper.mapTo(boardMemberRequestDto);
        boardMember.setRole(BoardRole.MEMBER);      // initial role is member for all

        BoardMember savedBoardMember = boardMemberRepository.save(boardMember);
        return boardMemberResponseMapper.mapTo(savedBoardMember);
    }

    @Override
    public void removeMember(Integer boardId, Integer memberUserId, Integer userId) {
        Board board = getBoard(boardId);

        if(!board.getCreatedById().equals(userId)) {
            throw new IllegalOperationException("You cannot remove member from this group");
        }

        if(board.getCreatedById().equals(memberUserId)) {
            throw new IllegalOperationException("Cannot remove board owner");
        }

        BoardMember boardMember = boardMemberRepository.findByBoardIdAndUserId(boardId, memberUserId)
                .orElseThrow(() -> new BoardMemberNotFoundException("Member is not in the board"));

        boardMemberRepository.delete(boardMember);
    }


    /*
    This should return user response dto so that angular can directly render all the users
    for that page else frontend needs to call this then for each response dto call user service
    and so much work to do by frontend we will simply create a bulk request and done
    here we cannot sort on the basis of name as sorting is done before pagination and we
    always need sorting otherwise pagination will not work so we are sorting on the basis
    of fields in board member as sorting in the basis of it needs all member then sort and
    returning will slow down as we will fetch all members.
     */
    @Override
    public CustomPageResponse<UserDto> getMembers(Integer boardId,
                                                                 Integer userId,
                                                                 Integer page,
                                                                 Integer size,
                                                                 String by,
                                                                 String direction) {
        if(!isMember(userId, boardId)) {
            throw new IllegalOperationException("You cannot view members of the board");
        }

        Sort sort = direction.equals("asc") ?
                Sort.by(by).ascending() :
                Sort.by(by).descending();

        Pageable pageable = PageRequest.of(page, size, sort);
        /*
           here we can definitely do this but then as we need to call user service to get userId
            we will then filter this to get only id so why to do here we will only fetch id from database
            to optimize speed, this should be done always ra
        */
        // Page<BoardMember> boardMemberPage = boardMemberRepository.findByBoardId(boardId, pageable);

        Page<Integer> userIdPage = boardMemberRepository.findUserIdsByBoardId(boardId, pageable);

        List<UserDto> userDtos = userClient.getUserBulk(userIdPage.getContent());

        return new CustomPageResponse<>(userIdPage, userDtos);
    }

    private boolean isMember(Integer userId, Integer boardId) {
        return boardMemberRepository.existsByBoardIdAndUserId(boardId, userId);
    }

    private Board getBoard(Integer boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new BoardNotFoundException("Board not found with id " + boardId.toString()));
    }
}