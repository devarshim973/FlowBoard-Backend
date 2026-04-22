package com.flowboard.workspace_service.service.impl;

import com.flowboard.workspace_service.dto.WorkspaceRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.entity.Workspace;
import com.flowboard.workspace_service.entity.WorkspaceMember;
import com.flowboard.workspace_service.exception.IllegalOperationException;
import com.flowboard.workspace_service.exception.WorkspaceNotFoundException;
import com.flowboard.workspace_service.mapper.Mapper;
import com.flowboard.workspace_service.repository.WorkspaceMemberRepository;
import com.flowboard.workspace_service.repository.WorkspaceRepository;
import com.flowboard.workspace_service.service.WorkspaceService;
import com.flowboard.workspace_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final Mapper<WorkspaceRequestDto, Workspace> workspaceRequestMapper;
    private final Mapper<Workspace, WorkspaceResponseDto> workspaceResponseMapper;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    public WorkspaceResponseDto createWorkspace(WorkspaceRequestDto workspaceRequestDto, Integer userId) {
        Workspace workspace = workspaceRequestMapper.mapTo(workspaceRequestDto);
        workspace.setOwnerId(userId);

        Workspace savedWorkspace = workspaceRepository.save(workspace);

        /*
        Also add owner as a member in the group
         */
        WorkspaceMember owner = WorkspaceMember
                .builder()
                .workspaceId(savedWorkspace.getWorkspaceId())
                .userId(userId)
                .build();

        workspaceMemberRepository.save(owner);
        return workspaceResponseMapper.mapTo(savedWorkspace);
    }

    @Override
    public WorkspaceResponseDto updateWorkspace(Integer workspaceId, WorkspaceRequestDto workspaceRequestDto, Integer userId) {
        validateAccess(workspaceId, userId);

        Workspace workspace = getWorkspace(workspaceId);

        workspace.setDescription(workspaceRequestDto.getDescription());
        workspace.setVisibility(workspaceRequestDto.getVisibility());
        workspace.setName(workspaceRequestDto.getName());
        workspace.setLogoUrl(workspaceRequestDto.getLogoUrl());

        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        return workspaceResponseMapper.mapTo(updatedWorkspace);
    }

    @Override
    public void deleteWorkspace(Integer workspaceId, Integer userId) {
        validateAccess(workspaceId, userId);

        Workspace workspace = getWorkspace(workspaceId);
        workspaceRepository.delete(workspace);
    }

    @Override
    public CustomPageResponse<WorkspaceResponseDto> getMyWorkspaces(Integer userId,
                                                                    Integer page,
                                                                    Integer size,
                                                                    String by,
                                                                    String direction) {
        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(by).ascending();
        else sort = Sort.by(by).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Workspace> workspacePage = workspaceRepository.findByOwnerId(userId, pageable);
        Page<WorkspaceResponseDto> workspaceResponseDtoPage = workspacePage
                .map(workspaceResponseMapper::mapTo);

        return new CustomPageResponse<>(workspaceResponseDtoPage);
    }

    private void validateAccess(Integer workspaceId, Integer userId) {
        Workspace workspace = getWorkspace(workspaceId);
        if(!workspace.getOwnerId().equals(userId)) {
            throw new IllegalOperationException("You are not allowed to make changes in this workspace");
        }
    }

    private Workspace getWorkspace(Integer id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found with id " + id.toString()));
    }
}
