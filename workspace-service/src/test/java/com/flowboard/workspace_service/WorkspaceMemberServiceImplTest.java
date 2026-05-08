package com.flowboard.workspace_service;

import com.flowboard.workspace_service.client.UserClient;
import com.flowboard.workspace_service.dto.WorkspaceMemberRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceMemberResponseDto;
import com.flowboard.workspace_service.entity.Visibility;
import com.flowboard.workspace_service.entity.Workspace;
import com.flowboard.workspace_service.entity.WorkspaceMember;
import com.flowboard.workspace_service.exception.IllegalOperationException;
import com.flowboard.workspace_service.exception.WorkspaceMemberNotFoundException;
import com.flowboard.workspace_service.exception.WorkspaceNotFoundException;
import com.flowboard.workspace_service.mapper.impl.WorkspaceMemberRequestMapper;
import com.flowboard.workspace_service.mapper.impl.WorkspaceMemberResponseMapper;
import com.flowboard.workspace_service.repository.WorkspaceMemberRepository;
import com.flowboard.workspace_service.repository.WorkspaceRepository;
import com.flowboard.workspace_service.service.impl.WorkspaceMemberServiceImpl;
import com.flowboard.workspace_service.util.CustomPageResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceMemberServiceImplTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @Mock
    private WorkspaceMemberRequestMapper workspaceMemberRequestMapper;

    @Mock
    private WorkspaceMemberResponseMapper workspaceMemberResponseMapper;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private WorkspaceMemberServiceImpl workspaceMemberService;

    private Workspace getWorkspace() {
        Workspace workspace = new Workspace();
        workspace.setWorkspaceId(1);
        workspace.setOwnerId(1);
        workspace.setVisibility(Visibility.PUBLIC);
        return workspace;
    }

    @Test
    void addMember_positive() {

        WorkspaceMemberRequestDto dto =
                new WorkspaceMemberRequestDto();
        dto.setWorkspaceId(1);
        dto.setUserId(2);

        WorkspaceMember member =
                WorkspaceMember.builder()
                        .workspaceId(1)
                        .userId(2)
                        .build();

        WorkspaceMemberResponseDto response =
                new WorkspaceMemberResponseDto();
        response.setWorkspaceId(1);

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(getWorkspace()));

        when(userClient.checkUser(2))
                .thenReturn(true);

        when(workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(1, 2))
                .thenReturn(false);

        // FIXED: create flow maps DTO -> entity
        when(workspaceMemberRequestMapper.mapTo(dto))
                .thenReturn(member);

        when(workspaceMemberRepository.save(any(WorkspaceMember.class)))
                .thenReturn(member);

        when(workspaceMemberResponseMapper.mapTo(member))
                .thenReturn(response);

        WorkspaceMemberResponseDto result =
                workspaceMemberService.addMember(dto, 1);

        assertEquals(1, result.getWorkspaceId());
    }

    @Test
    void addMember_userNotFound_negative() {

        WorkspaceMemberRequestDto dto =
                new WorkspaceMemberRequestDto();
        dto.setWorkspaceId(1);
        dto.setUserId(2);

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(getWorkspace()));

        when(userClient.checkUser(2))
                .thenReturn(false);

        assertThrows(IllegalOperationException.class,
                () -> workspaceMemberService.addMember(dto, 1));
    }

    @Test
    void addMember_alreadyExists_negative() {

        WorkspaceMemberRequestDto dto =
                new WorkspaceMemberRequestDto();
        dto.setWorkspaceId(1);
        dto.setUserId(2);

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(getWorkspace()));

        when(userClient.checkUser(2))
                .thenReturn(true);

        when(workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(1, 2))
                .thenReturn(true);

        assertThrows(IllegalOperationException.class,
                () -> workspaceMemberService.addMember(dto, 1));
    }

    @Test
    void removeMember_positive() {

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(getWorkspace()));

        when(workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(1, 2))
                .thenReturn(true);

        workspaceMemberService.removeMember(1, 2, 1);

        verify(workspaceMemberRepository)
                .deleteByWorkspaceIdAndUserId(1, 2);
    }

    @Test
    void removeMember_negative() {

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(getWorkspace()));

        when(workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(1, 2))
                .thenReturn(false);

        assertThrows(WorkspaceMemberNotFoundException.class,
                () -> workspaceMemberService.removeMember(1, 2, 1));
    }

    @Test
    void getMembers_positive() {

        WorkspaceMember member =
                WorkspaceMember.builder()
                        .workspaceId(1)
                        .userId(2)
                        .build();

        PageImpl<WorkspaceMember> page =
                new PageImpl<>(
                        List.of(member),
                        PageRequest.of(0, 5),
                        1
                );

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(getWorkspace()));

        when(workspaceMemberRepository
                .findByWorkspaceId(anyInt(), any()))
                .thenReturn(page);

        when(workspaceMemberResponseMapper
                .mapTo(any(WorkspaceMember.class)))
                .thenReturn(new WorkspaceMemberResponseDto());

        CustomPageResponse<WorkspaceMemberResponseDto> result =
                workspaceMemberService.getMembers(
                        1, 1, 0, 5, "id", "asc"
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void accessDenied_negative() {

        Workspace workspace = getWorkspace();
        workspace.setOwnerId(9);

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(workspace));

        assertThrows(IllegalOperationException.class,
                () -> workspaceMemberService.getMembers(
                        1, 1, 0, 5, "id", "asc"
                ));
    }

    @Test
    void workspaceNotFound_negative() {

        when(workspaceRepository.findById(99))
                .thenReturn(Optional.empty());

        WorkspaceMemberRequestDto dto =
                new WorkspaceMemberRequestDto();
        dto.setWorkspaceId(99);
        dto.setUserId(2);

        assertThrows(WorkspaceNotFoundException.class,
                () -> workspaceMemberService.addMember(dto, 1));
    }
}