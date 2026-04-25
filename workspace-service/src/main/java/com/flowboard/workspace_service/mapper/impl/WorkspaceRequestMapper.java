package com.flowboard.workspace_service.mapper.impl;

import com.flowboard.workspace_service.dto.WorkspaceRequestDto;
import com.flowboard.workspace_service.entity.Workspace;
import com.flowboard.workspace_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceRequestMapper implements Mapper<WorkspaceRequestDto, Workspace> {
    private final ModelMapper modelMapper;

    @Override
    public Workspace mapTo(WorkspaceRequestDto workspaceRequestDto) {
        return modelMapper.map(workspaceRequestDto, Workspace.class);
    }

    @Override
    public WorkspaceRequestDto mapFrom(Workspace workspace) {
        return modelMapper.map(workspace, WorkspaceRequestDto.class);
    }
}
