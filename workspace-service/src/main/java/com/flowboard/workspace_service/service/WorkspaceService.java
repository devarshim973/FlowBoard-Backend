package com.flowboard.workspace_service.service;

import com.flowboard.workspace_service.dto.WorkspaceRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.util.CustomPageResponse;

public interface WorkspaceService {

    public WorkspaceResponseDto createWorkspace(WorkspaceRequestDto dto, Integer userId);

    public WorkspaceResponseDto updateWorkspace(Integer workspaceId, WorkspaceRequestDto dto, Integer userId);

    public void deleteWorkspace(Integer workspaceId, Integer userId);

    public CustomPageResponse<WorkspaceResponseDto> getMyWorkspaces(Integer userId,
                                                                    Integer page,
                                                                    Integer size,
                                                                    String by,
                                                                    String direction);

    public Integer getOwenerId(Integer id);

    public CustomPageResponse<WorkspaceResponseDto> getPublicWorkspace(int page, int size, String by, String direction);

    public Boolean isMember(Integer workspaceId, Integer memberId);

    public Boolean isPrivate(Integer workspaceId);

    CustomPageResponse<WorkspaceResponseDto> getJoinedWorkspaces(Integer ownerId, int page, int size, String by, String direction);

    WorkspaceResponseDto findById(Integer workspaceId, Integer userid);

    Boolean checkModificationAccess(Integer workspaceId, Integer userId);
}