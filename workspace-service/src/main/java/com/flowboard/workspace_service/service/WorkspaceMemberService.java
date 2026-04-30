package com.flowboard.workspace_service.service;

import com.flowboard.workspace_service.dto.WorkspaceMemberRequestDto;
import com.flowboard.workspace_service.dto.WorkspaceMemberResponseDto;
import com.flowboard.workspace_service.util.CustomPageResponse;

public interface WorkspaceMemberService {
    public WorkspaceMemberResponseDto addMember(WorkspaceMemberRequestDto workspaceMemberRequestDto, Integer ownerId);

    public void removeMember(Integer workspaceId, Integer userId, Integer ownerId);

    public CustomPageResponse<WorkspaceMemberResponseDto> getMembers(Integer workspaceId,
                                                                     Integer ownerId,
                                                                     Integer page,
                                                                     Integer size,
                                                                     String by,
                                                                     String direction);
}