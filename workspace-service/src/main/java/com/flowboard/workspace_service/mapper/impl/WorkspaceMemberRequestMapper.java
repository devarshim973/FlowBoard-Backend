package com.flowboard.workspace_service.mapper.impl;

import com.flowboard.workspace_service.dto.WorkspaceMemberRequestDto;
import com.flowboard.workspace_service.entity.WorkspaceMember;
import com.flowboard.workspace_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceMemberRequestMapper implements Mapper<WorkspaceMemberRequestDto, WorkspaceMember> {
    private final ModelMapper modelMapper;

    @Override
    public WorkspaceMember mapTo(WorkspaceMemberRequestDto workspaceMemberRequestDto) {
        return modelMapper.map(workspaceMemberRequestDto, WorkspaceMember.class);
    }

    @Override
    public WorkspaceMemberRequestDto mapFrom(WorkspaceMember workspaceMember) {
        return modelMapper.map(workspaceMember, WorkspaceMemberRequestDto.class);
    }
}
