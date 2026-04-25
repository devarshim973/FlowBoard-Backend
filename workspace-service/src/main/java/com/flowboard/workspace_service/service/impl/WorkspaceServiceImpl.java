package com.flowboard.workspace_service.service.impl;

import com.flowboard.workspace_service.dto.WorkspaceRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.entity.Visibility;
import com.flowboard.workspace_service.entity.Workspace;
import com.flowboard.workspace_service.entity.WorkspaceMember;
import com.flowboard.workspace_service.exception.IllegalOperationException;
import com.flowboard.workspace_service.exception.WorkspaceNotFoundException;
import com.flowboard.workspace_service.mapper.Mapper;
import com.flowboard.workspace_service.mapper.impl.WorkspaceRequestMapper;
import com.flowboard.workspace_service.mapper.impl.WorkspaceResponseMapper;
import com.flowboard.workspace_service.repository.WorkspaceMemberRepository;
import com.flowboard.workspace_service.repository.WorkspaceRepository;
import com.flowboard.workspace_service.service.WorkspaceService;
import com.flowboard.workspace_service.util.AppConstants;
import com.flowboard.workspace_service.util.CustomPageResponse;
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
public class WorkspaceServiceImpl implements WorkspaceService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceRequestMapper workspaceRequestMapper;
    private final WorkspaceResponseMapper workspaceResponseMapper;
    private final WorkspaceMemberRepository workspaceMemberRepository;

    @Override
    public WorkspaceResponseDto createWorkspace(WorkspaceRequestDto workspaceRequestDto, Integer userId) {
        log.info("Create workspace requested by user {}", userId);
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
        log.info("Workspace created with id {}", savedWorkspace.getWorkspaceId());
        return workspaceResponseMapper.mapTo(savedWorkspace);
    }

    @Override
    public WorkspaceResponseDto updateWorkspace(Integer workspaceId, WorkspaceRequestDto workspaceRequestDto, Integer userId) {
        log.info("Update workspace requested for workspace {} by user {}", workspaceId, userId);
        validateAccess(workspaceId, userId);

        Workspace workspace = getWorkspace(workspaceId);

        workspace.setDescription(workspaceRequestDto.getDescription());
        workspace.setVisibility(workspaceRequestDto.getVisibility());
        workspace.setName(workspaceRequestDto.getName());
        workspace.setLogoUrl(workspaceRequestDto.getLogoUrl());

        Workspace updatedWorkspace = workspaceRepository.save(workspace);
        log.info("Workspace updated with id {}", updatedWorkspace.getWorkspaceId());
        return workspaceResponseMapper.mapTo(updatedWorkspace);
    }

    @Override
    public void deleteWorkspace(Integer workspaceId, Integer userId) {
        log.info("Delete workspace requested for workspace {} by user {}", workspaceId, userId);
        validateAccess(workspaceId, userId);

        Workspace workspace = getWorkspace(workspaceId);
        workspaceRepository.delete(workspace);
        log.info("Workspace deleted with id {}", workspaceId);
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

    @Override
    public CustomPageResponse<WorkspaceResponseDto> getJoinedWorkspaces(Integer userId,
                                                                        int page,
                                                                        int size,
                                                                        String by,
                                                                        String direction) {

        Sort sortWorkspaceMember;
        if ("asc".equalsIgnoreCase(direction)) {
            sortWorkspaceMember = Sort.by(AppConstants.sortForWorkspaceMember).ascending();
        }
        else {
            sortWorkspaceMember = Sort.by(AppConstants.sortForWorkspaceMember).descending();
        }

        Pageable pageable = PageRequest.of(page, size, sortWorkspaceMember);

        Page<WorkspaceMember> workspaceMemberPage =
                workspaceMemberRepository.findByUserId(userId, pageable);

        List<Integer> workspaceIds = workspaceMemberPage.getContent()
                .stream()
                .map(WorkspaceMember::getWorkspaceId)
                .toList();

        if (workspaceIds.isEmpty()) {
            return new CustomPageResponse<>(Page.empty(pageable));
        }

        Sort sortWorkspace;
        if ("asc".equalsIgnoreCase(direction)) {
            sortWorkspace = Sort.by(by).ascending();
        }
        else {
            sortWorkspace = Sort.by(by).descending();
        }

        pageable = PageRequest.of(page, size, sortWorkspace);

        Page<Workspace> workspacePage =
                workspaceRepository.findByWorkspaceIdIn(workspaceIds, pageable);

        Page<WorkspaceResponseDto> workspaceResponseDtoPage =
                workspacePage.map(workspaceResponseMapper::mapTo);

        return new CustomPageResponse<>(workspaceResponseDtoPage);
    }

    @Override
    public WorkspaceResponseDto findById(Integer workspaceId, Integer userId) {
        validateAccess(workspaceId, userId);
        return workspaceResponseMapper.mapTo(getWorkspace(workspaceId));
    }

    @Override
    public Boolean checkModificationAccess(Integer workspaceId, Integer userId) {
        try{
            validateAccess(workspaceId, userId);
        }
        catch (IllegalOperationException ex) {
            log.warn("Workspace modification denied for workspace {} and user {}", workspaceId, userId);
            return false;
        }
        return true;
    }

    private void validateAccess(Integer workspaceId, Integer userId) {
        Workspace workspace = getWorkspace(workspaceId);
        if(!workspace.getOwnerId().equals(userId)) {
            throw new IllegalOperationException("You are not allowed to make changes in this workspace");
        }
    }

    @Override
    public CustomPageResponse<WorkspaceResponseDto> getPublicWorkspace(int page, int size, String by, String direction) {
        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(by).ascending();
        else sort = Sort.by(by).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Workspace> workspacePage = workspaceRepository.findByVisibility(Visibility.PUBLIC, pageable);
        Page<WorkspaceResponseDto> workspaceResponseDtoPage = workspacePage
                .map(workspaceResponseMapper::mapTo);

        return new CustomPageResponse<>(workspaceResponseDtoPage);
    }

    private Workspace getWorkspace(Integer id) {
        return workspaceRepository.findById(id)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found with id " + id.toString()));
    }

    @Override
    public Integer getOwenerId(Integer id) {
        return getWorkspace(id).getOwnerId();
    }

    @Override
    public Boolean isMember(Integer workspaceId, Integer memberId) {
        log.info("Checking membership for user {} in workspace {}", memberId, workspaceId);
        return workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, memberId);
    }

    @Override
    public Boolean isPrivate(Integer workspaceId) {
        return getWorkspace(workspaceId).getVisibility().equals(Visibility.PRIVATE);
    }
}
