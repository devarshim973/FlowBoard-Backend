package com.flowboard.board_service;

import com.flowboard.board_service.config.AppConfig;
import com.flowboard.board_service.controller.AdminBoardController;
import com.flowboard.board_service.dto.BoardMemberRequestDto;
import com.flowboard.board_service.dto.BoardMemberResponseDto;
import com.flowboard.board_service.dto.BoardRequestDto;
import com.flowboard.board_service.dto.BoardResponseDto;
import com.flowboard.board_service.dto.UserDto;
import com.flowboard.board_service.entity.Board;
import com.flowboard.board_service.entity.BoardMember;
import com.flowboard.board_service.entity.BoardRole;
import com.flowboard.board_service.entity.Visibility;
import com.flowboard.board_service.exception.BoardMemberNotFoundException;
import com.flowboard.board_service.exception.BoardNotFoundException;
import com.flowboard.board_service.exception.GlobalExceptionHandler;
import com.flowboard.board_service.exception.IllegalOperationException;
import com.flowboard.board_service.exception.ServiceUnavailableException;
import com.flowboard.board_service.fallback.UserFallback;
import com.flowboard.board_service.fallback.WorkspaceFallback;
import com.flowboard.board_service.mapper.impl.BoardMemberRequestMapper;
import com.flowboard.board_service.mapper.impl.BoardMemberResponseMapper;
import com.flowboard.board_service.mapper.impl.BoardRequestMapper;
import com.flowboard.board_service.mapper.impl.BoardResponseMapper;
import com.flowboard.board_service.service.BoardService;
import com.flowboard.board_service.util.CustomPageResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class BoardInfrastructureTest {

    private final AppConfig appConfig = new AppConfig();
    private final ModelMapper modelMapper = appConfig.modelMapper();

    @Test
    void adminControllerAllowsAdminRequests() {
        BoardService boardService = mock(BoardService.class);
        AdminBoardController controller = new AdminBoardController(boardService);

        BoardResponseDto board = new BoardResponseDto();
        board.setBoardId(11);
        CustomPageResponse<BoardResponseDto> page =
                new CustomPageResponse<>(new PageImpl<>(List.of(board), PageRequest.of(0, 5), 1));

        when(boardService.getAllBoards(0, 5, "name", "asc")).thenReturn(page);

        ResponseEntity<CustomPageResponse<BoardResponseDto>> response =
                controller.getAllBoards("admin,member", 0, 5, "name", "asc");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getContent().size());
        assertEquals(11, response.getBody().getContent().get(0).getBoardId());
        verify(boardService).getAllBoards(0, 5, "name", "asc");
    }

    @Test
    void adminControllerDeletesBoardForPlatformAdmin() {
        BoardService boardService = mock(BoardService.class);
        AdminBoardController controller = new AdminBoardController(boardService);

        ResponseEntity<String> response = controller.deleteBoard("PLATFORM_ADMIN", 9);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Board deleted successfully", response.getBody());
        verify(boardService).deleteBoardAsAdmin(9);
    }

    @Test
    void adminControllerRejectsNonAdminRequests() {
        BoardService boardService = mock(BoardService.class);
        AdminBoardController controller = new AdminBoardController(boardService);

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> controller.getAllBoards("member", 0, 5, "name", "asc")
        );

        assertEquals("Admin access required", exception.getMessage());
        verifyNoInteractions(boardService);
    }

    @Test
    void appConfigBuildsStrictMapperAndAddsHeaderParameter() {
        assertEquals(MatchingStrategies.STRICT, modelMapper.getConfiguration().getMatchingStrategy());
        assertNotNull(appConfig.customOpenAPI());

        OpenAPI openAPI = new OpenAPI();
        Operation operation = new Operation();
        PathItem pathItem = new PathItem().get(operation);
        openAPI.path("/boards", pathItem);

        appConfig.customizer().customise(openAPI);

        assertNotNull(operation.getParameters());
        assertEquals(1, operation.getParameters().size());
        assertEquals("X-User-Id", operation.getParameters().get(0).getName());
        assertTrue(Boolean.TRUE.equals(operation.getParameters().get(0).getRequired()));
    }

    @Test
    void fallbackClassesReturnExpectedValues() {
        UserFallback userFallback = new UserFallback();
        assertNull(userFallback.getUserBulk(List.of(1, 2, 3)));

        WorkspaceFallback workspaceFallback = new WorkspaceFallback();
        assertEquals(
                "Workspace service not available",
                assertThrows(ServiceUnavailableException.class, () -> workspaceFallback.getOwnerId(1)).getMessage()
        );
        assertEquals(
                "Workspace service not available",
                assertThrows(ServiceUnavailableException.class, () -> workspaceFallback.isMember(1, 2)).getMessage()
        );
        assertEquals(
                "Workspace service not available",
                assertThrows(ServiceUnavailableException.class, () -> workspaceFallback.isPrivate(1)).getMessage()
        );
    }

    @Test
    void globalExceptionHandlerMapsExpectedStatuses() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        assertEquals(
                HttpStatus.BAD_REQUEST,
                handler.handleIllegalOperationException(new IllegalOperationException("illegal")).getStatusCode()
        );
        assertEquals(
                "missing board",
                handler.handleBoardNotFoundException(new BoardNotFoundException("missing board")).getBody()
        );
        assertEquals(
                "missing member",
                handler.handleBoardMemberNotFoundException(new BoardMemberNotFoundException("missing member")).getBody()
        );
        assertEquals(
                HttpStatus.INTERNAL_SERVER_ERROR,
                handler.handleServiceUnavailableException(new ServiceUnavailableException("down")).getStatusCode()
        );
        assertEquals(
                "generic",
                handler.handleGenericException(new Exception("generic")).getBody()
        );
    }

    @Test
    void boardMappersMapBothDirections() {
        BoardRequestDto requestDto = new BoardRequestDto();
        requestDto.setWorkspaceId(10);
        requestDto.setName("Platform");
        requestDto.setDescription("Board description");
        requestDto.setBackground("bg");
        requestDto.setVisibility(Visibility.PUBLIC);

        BoardRequestMapper requestMapper = new BoardRequestMapper(modelMapper);
        Board board = requestMapper.mapTo(requestDto);
        assertEquals(10, board.getWorkspaceId());
        assertEquals("Platform", board.getName());
        assertEquals(Visibility.PUBLIC, board.getVisibility());

        Board boardEntity = new Board();
        boardEntity.setBoardId(5);
        boardEntity.setWorkspaceId(10);
        boardEntity.setName("Delivery");
        boardEntity.setDescription("Desc");
        boardEntity.setBackground("blue");
        boardEntity.setVisibility(Visibility.PRIVATE);
        boardEntity.setCreatedById(7);
        boardEntity.setClosed(true);
        boardEntity.setCreatedAt(LocalDateTime.now().minusDays(1));
        boardEntity.setUpdatedAt(LocalDateTime.now());

        BoardResponseMapper responseMapper = new BoardResponseMapper(modelMapper);
        BoardResponseDto responseDto = responseMapper.mapTo(boardEntity);
        assertEquals(5, responseDto.getBoardId());
        assertEquals("Delivery", responseDto.getName());
        assertTrue(responseDto.isClosed());

        Board mappedBack = responseMapper.mapFrom(responseDto);
        assertEquals(7, mappedBack.getCreatedById());
        assertTrue(mappedBack.isClosed());
    }

    @Test
    void boardMemberMappersMapBothDirections() {
        BoardMemberRequestDto requestDto = new BoardMemberRequestDto();
        requestDto.setBoardId(3);
        requestDto.setUserId(8);

        BoardMemberRequestMapper requestMapper = new BoardMemberRequestMapper(modelMapper);
        BoardMember member = requestMapper.mapTo(requestDto);
        assertEquals(3, member.getBoardId());
        assertEquals(8, member.getUserId());

        BoardMember memberEntity = BoardMember.builder()
                .boardMemberId(4)
                .boardId(3)
                .userId(8)
                .role(BoardRole.OBSERVER)
                .addedAt(LocalDateTime.now())
                .build();

        BoardMemberResponseMapper responseMapper = new BoardMemberResponseMapper(modelMapper);
        BoardMemberResponseDto responseDto = responseMapper.mapTo(memberEntity);
        assertEquals(4, responseDto.getBoardMemberId());
        assertEquals(BoardRole.OBSERVER, responseDto.getRole());

        BoardMember mappedBack = responseMapper.mapFrom(responseDto);
        assertEquals(8, mappedBack.getUserId());
        assertEquals(BoardRole.OBSERVER, mappedBack.getRole());
        assertFalse(mappedBack.getBoardId() == null);
    }

    @Test
    void applicationAnnotationsRemainPresent() {
        assertTrue(BoardServiceApplication.class.isAnnotationPresent(org.springframework.boot.autoconfigure.SpringBootApplication.class));
        assertTrue(BoardServiceApplication.class.isAnnotationPresent(org.springframework.cloud.openfeign.EnableFeignClients.class));
    }
}
