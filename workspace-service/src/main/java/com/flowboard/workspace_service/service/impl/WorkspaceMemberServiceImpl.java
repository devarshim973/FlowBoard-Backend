package com.flowboard.workspace_service.service.impl;

import com.flowboard.workspace_service.dto.WorkspaceMemberRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceMemberResponseDto;
import com.flowboard.workspace_service.entity.Workspace;
import com.flowboard.workspace_service.entity.WorkspaceMember;
import com.flowboard.workspace_service.exception.IllegalOperationException;
import com.flowboard.workspace_service.exception.WorkspaceMemberNotFoundException;
import com.flowboard.workspace_service.exception.WorkspaceNotFoundException;
import com.flowboard.workspace_service.mapper.Mapper;
import com.flowboard.workspace_service.repository.WorkspaceMemberRepository;
import com.flowboard.workspace_service.repository.WorkspaceRepository;
import com.flowboard.workspace_service.service.WorkspaceMemberService;
import com.flowboard.workspace_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class WorkspaceMemberServiceImpl implements WorkspaceMemberService {
    private final WorkspaceRepository workspaceRepository;
    private final WorkspaceMemberRepository workspaceMemberRepository;
    private final Mapper<WorkspaceMemberRequestDto, WorkspaceMember> workspaceMemberRequestMapper;
    private final Mapper<WorkspaceMember, WorkspaceMemberResponseDto> workspaceMemberResponseMapper;

    @Override
    public WorkspaceMemberResponseDto addMember(WorkspaceMemberRequestDto workspaceMemberRequestDto, Integer ownerId) {
        validateAccess(workspaceMemberRequestDto.getWorkspaceId(), ownerId);

        Integer workspaceId = workspaceMemberRequestDto.getWorkspaceId();
        Integer userId = workspaceMemberRequestDto.getUserId();

        if(workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new IllegalOperationException("Already member");
        }

        WorkspaceMember workspaceMember = workspaceMemberRequestMapper.mapTo(workspaceMemberRequestDto);

        WorkspaceMember savedMember = workspaceMemberRepository.save(workspaceMember);
        return workspaceMemberResponseMapper.mapTo(savedMember);
    }

    @Override
    @Transactional
    public void removeMember(Integer workspaceId, Integer userId, Integer ownerId) {
        validateAccess(workspaceId, ownerId);

        if(!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            throw new WorkspaceMemberNotFoundException("No member found with workspace id " + workspaceId + " user id as " + userId);
        }

        workspaceMemberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
    }

    @Override
    public CustomPageResponse<WorkspaceMemberResponseDto> getMembers(Integer workspaceId,
                                                                     Integer userId,
                                                                     Integer page,
                                                                     Integer size,
                                                                     String by,
                                                                     String direction) {
        validateAccess(workspaceId, userId);

        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(by).ascending();
        else sort = Sort.by(by).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<WorkspaceMember> workspaceMemberPage = workspaceMemberRepository
                .findByWorkspaceId(workspaceId, pageable);

        Page<WorkspaceMemberResponseDto> workspaceMemberResponseDtoPage = workspaceMemberPage
                .map(workspaceMemberResponseMapper::mapTo);

        return new CustomPageResponse<>(workspaceMemberResponseDtoPage);
    }

    private void validateAccess(Integer workspaceId, Integer userId) {
        Workspace workspace = getWorkspace(workspaceId);
        if(!workspace.getOwnerId().equals(userId)) {
            throw new IllegalOperationException("You are not allowed to make changes in this workspace");
        }
    }

    private Workspace getWorkspace(Integer workspaceId) {
        return workspaceRepository.findById(workspaceId)
                .orElseThrow(() -> new WorkspaceNotFoundException("Workspace not found with id " + workspaceId.toString()));
    }
}
