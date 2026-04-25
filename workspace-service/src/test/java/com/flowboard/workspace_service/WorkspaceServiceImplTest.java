package com.flowboard.workspace_service;

import com.flowboard.workspace_service.dto.WorkspaceRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.entity.Visibility;
import com.flowboard.workspace_service.entity.Workspace;
import com.flowboard.workspace_service.entity.WorkspaceMember;
import com.flowboard.workspace_service.exception.IllegalOperationException;
import com.flowboard.workspace_service.exception.WorkspaceNotFoundException;
import com.flowboard.workspace_service.mapper.impl.WorkspaceRequestMapper;
import com.flowboard.workspace_service.mapper.impl.WorkspaceResponseMapper;
import com.flowboard.workspace_service.repository.WorkspaceMemberRepository;
import com.flowboard.workspace_service.repository.WorkspaceRepository;
import com.flowboard.workspace_service.service.impl.WorkspaceServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkspaceServiceImplTest {

    @Mock
    private WorkspaceRepository workspaceRepository;

    @Mock
    private WorkspaceRequestMapper workspaceRequestMapper;

    @Mock
    private WorkspaceResponseMapper workspaceResponseMapper;

    @Mock
    private WorkspaceMemberRepository workspaceMemberRepository;

    @InjectMocks
    private WorkspaceServiceImpl workspaceService;

    private Workspace getWorkspace() {
        Workspace workspace = new Workspace();
        workspace.setWorkspaceId(1);
        workspace.setOwnerId(1);
        workspace.setName("FlowBoard");
        workspace.setVisibility(Visibility.PUBLIC);
        return workspace;
    }

    @Test
    void createWorkspace_positive() {

        WorkspaceRequestDto dto =
                new WorkspaceRequestDto();

        Workspace workspace = getWorkspace();

        WorkspaceResponseDto response =
                new WorkspaceResponseDto();
        response.setWorkspaceId(1);

        when(workspaceRequestMapper.mapTo(dto))
                .thenReturn(workspace);

        when(workspaceRepository.save(any(Workspace.class)))
                .thenReturn(workspace);

        when(workspaceResponseMapper.mapTo(workspace))
                .thenReturn(response);

        WorkspaceResponseDto result =
                workspaceService.createWorkspace(dto, 1);

        assertEquals(1, result.getWorkspaceId());

        verify(workspaceMemberRepository)
                .save(any(WorkspaceMember.class));
    }

    @Test
    void updateWorkspace_positive() {

        WorkspaceRequestDto dto =
                new WorkspaceRequestDto();

        Workspace workspace = getWorkspace();

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(workspace));

        when(workspaceRepository.save(any(Workspace.class)))
                .thenReturn(workspace);

        when(workspaceResponseMapper.mapTo(workspace))
                .thenReturn(new WorkspaceResponseDto());

        workspaceService.updateWorkspace(1, dto, 1);
    }

    @Test
    void updateWorkspace_negative() {

        Workspace workspace = getWorkspace();
        workspace.setOwnerId(5);

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(workspace));

        assertThrows(IllegalOperationException.class,
                () -> workspaceService.updateWorkspace(
                        1,
                        new WorkspaceRequestDto(),
                        1
                ));
    }

    @Test
    void deleteWorkspace_positive() {

        Workspace workspace = getWorkspace();

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(workspace));

        workspaceService.deleteWorkspace(1, 1);

        verify(workspaceRepository).delete(workspace);
    }

    @Test
    void getMyWorkspaces_positive() {

        PageImpl<Workspace> page =
                new PageImpl<>(
                        List.of(getWorkspace()),
                        PageRequest.of(0, 5),
                        1
                );

        when(workspaceRepository.findByOwnerId(
                anyInt(),
                any()))
                .thenReturn(page);

        when(workspaceResponseMapper.mapTo(any(Workspace.class)))
                .thenReturn(new WorkspaceResponseDto());

        CustomPageResponse<WorkspaceResponseDto> result =
                workspaceService.getMyWorkspaces(
                        1, 0, 5, "workspaceId", "asc"
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getJoinedWorkspaces_positive() {

        WorkspaceMember member =
                WorkspaceMember.builder()
                        .workspaceId(1)
                        .userId(1)
                        .build();

        PageImpl<WorkspaceMember> memberPage =
                new PageImpl<>(
                        List.of(member),
                        PageRequest.of(0, 5),
                        1
                );

        PageImpl<Workspace> workspacePage =
                new PageImpl<>(
                        List.of(getWorkspace()),
                        PageRequest.of(0, 5),
                        1
                );

        when(workspaceMemberRepository.findByUserId(
                anyInt(),
                any()))
                .thenReturn(memberPage);

        when(workspaceRepository.findByWorkspaceIdIn(
                anyList(),
                any()))
                .thenReturn(workspacePage);

        when(workspaceResponseMapper.mapTo(any(Workspace.class)))
                .thenReturn(new WorkspaceResponseDto());

        CustomPageResponse<WorkspaceResponseDto> result =
                workspaceService.getJoinedWorkspaces(
                        1, 0, 5, "workspaceId", "asc"
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void findById_positive() {

        Workspace workspace = getWorkspace();

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(workspace));

        when(workspaceResponseMapper.mapTo(workspace))
                .thenReturn(new WorkspaceResponseDto());

        workspaceService.findById(1, 1);
    }

    @Test
    void findById_negative() {

        Workspace workspace = getWorkspace();
        workspace.setOwnerId(5);

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(workspace));

        assertThrows(IllegalOperationException.class,
                () -> workspaceService.findById(1, 1));
    }

    @Test
    void checkModificationAccess_positive() {

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(getWorkspace()));

        assertEquals(true,
                workspaceService.checkModificationAccess(1, 1));
    }

    @Test
    void getPublicWorkspace_positive() {

        PageImpl<Workspace> page =
                new PageImpl<>(
                        List.of(getWorkspace()),
                        PageRequest.of(0, 5),
                        1
                );

        when(workspaceRepository.findByVisibility(
                any(Visibility.class),
                any()))
                .thenReturn(page);

        when(workspaceResponseMapper.mapTo(any(Workspace.class)))
                .thenReturn(new WorkspaceResponseDto());

        CustomPageResponse<WorkspaceResponseDto> result =
                workspaceService.getPublicWorkspace(
                        0, 5, "workspaceId", "asc"
                );

        assertEquals(1, result.getContent().size());
    }

    @Test
    void getOwenerId_positive() {

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(getWorkspace()));

        assertEquals(1,
                workspaceService.getOwenerId(1));
    }

    @Test
    void isMember_positive() {

        when(workspaceMemberRepository
                .existsByWorkspaceIdAndUserId(1, 2))
                .thenReturn(true);

        assertEquals(true,
                workspaceService.isMember(1, 2));
    }

    @Test
    void isPrivate_positive() {

        Workspace workspace = getWorkspace();
        workspace.setVisibility(Visibility.PRIVATE);

        when(workspaceRepository.findById(1))
                .thenReturn(Optional.of(workspace));

        assertEquals(true,
                workspaceService.isPrivate(1));
    }

    @Test
    void workspaceNotFound_negative() {

        when(workspaceRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(WorkspaceNotFoundException.class,
                () -> workspaceService.getOwenerId(99));
    }
}