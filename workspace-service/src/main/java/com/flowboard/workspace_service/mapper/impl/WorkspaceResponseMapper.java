package com.flowboard.workspace_service.mapper.impl;

import com.flowboard.workspace_service.dto.WorkspaceResponseDto;
import com.flowboard.workspace_service.entity.Workspace;
import com.flowboard.workspace_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceResponseMapper implements Mapper<Workspace, WorkspaceResponseDto> {
    private final ModelMapper modelMapper;

    @Override
    public WorkspaceResponseDto mapTo(Workspace workspace) {
        return modelMapper.map(workspace, WorkspaceResponseDto.class);
    }

    @Override
    public Workspace mapFrom(WorkspaceResponseDto workspaceResponseDto) {
        return modelMapper.map(workspaceResponseDto, Workspace.class);
    }
}
