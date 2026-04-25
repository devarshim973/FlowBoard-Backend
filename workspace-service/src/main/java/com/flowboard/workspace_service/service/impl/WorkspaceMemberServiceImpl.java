package com.flowboard.workspace_service.service.impl;

import com.flowboard.workspace_service.client.UserClient;
import com.flowboard.workspace_service.dto.WorkspaceMemberRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceMemberResponseDto;
import com.flowboard.workspace_service.entity.Workspace;
import com.flowboard.workspace_service.entity.WorkspaceMember;
import com.flowboard.workspace_service.exception.IllegalOperationException;
import com.flowboard.workspace_service.exception.WorkspaceMemberNotFoundException;
import com.flowboard.workspace_service.exception.WorkspaceNotFoundException;
import com.flowboard.workspace_service.mapper.Mapper;
import com.flowboard.workspace_service.mapper.impl.WorkspaceMemberRequestMapper;
import com.flowboard.workspace_service.mapper.impl.WorkspaceMemberResponseMapper;
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
    private final WorkspaceMemberRequestMapper workspaceMemberRequestMapper;
    private final WorkspaceMemberResponseMapper workspaceMemberResponseMapper;
    private final UserClient userClient;

    @Override
    public WorkspaceMemberResponseDto addMember(WorkspaceMemberRequestDto workspaceMemberRequestDto, Integer loggedUseId) {
        log.info("Add workspace member requested for workspace {} by user {}", workspaceMemberRequestDto.getWorkspaceId(), loggedUseId);
        validateAccess(workspaceMemberRequestDto.getWorkspaceId(), loggedUseId);

        Integer workspaceId = workspaceMemberRequestDto.getWorkspaceId();
        Integer userId = workspaceMemberRequestDto.getUserId();

        boolean isUserValid = userClient.checkUser(userId);

        if(!isUserValid) {
            log.warn("Workspace member add failed because user {} does not exist", userId);
            throw new IllegalOperationException("User does not exist");
        }

        if(workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            log.warn("Workspace member add skipped because user {} is already in workspace {}", userId, workspaceId);
            throw new IllegalOperationException("Already member");
        }

        WorkspaceMember workspaceMember = workspaceMemberRequestMapper.mapTo(workspaceMemberRequestDto);

        WorkspaceMember savedMember = workspaceMemberRepository.save(workspaceMember);
        log.info("Member added to workspace {}", workspaceId);
        return workspaceMemberResponseMapper.mapTo(savedMember);
    }

    @Override
    @Transactional
    public void removeMember(Integer workspaceId, Integer userId, Integer loggedUseId) {
        log.info("Remove workspace member requested for workspace {} by user {}", workspaceId, loggedUseId);
        validateAccess(workspaceId, loggedUseId);

        if(!workspaceMemberRepository.existsByWorkspaceIdAndUserId(workspaceId, userId)) {
            log.warn("Workspace member removal failed because user {} is not in workspace {}", userId, workspaceId);
            throw new WorkspaceMemberNotFoundException("No member found with workspace id " + workspaceId + " user id as " + userId);
        }

        workspaceMemberRepository.deleteByWorkspaceIdAndUserId(workspaceId, userId);
        log.info("Member {} removed from workspace {}", userId, workspaceId);
    }

    @Override
    public CustomPageResponse<WorkspaceMemberResponseDto> getMembers(Integer workspaceId,
                                                                     Integer loggedUseId,
                                                                     Integer page,
                                                                     Integer size,
                                                                     String by,
                                                                     String direction) {
        validateAccess(workspaceId, loggedUseId);

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
        log.info("Validating workspace member access for user {} in workspace {}", userId, workspaceId);
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
