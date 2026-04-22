package com.flowboard.workspace_service.mapper.impl;

import com.flowboard.workspace_service.dto.WorkspaceMemberResponseDto;
import com.flowboard.workspace_service.entity.WorkspaceMember;
import com.flowboard.workspace_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WorkspaceMemberResponseMapper implements Mapper<WorkspaceMember, WorkspaceMemberResponseDto> {
    private final ModelMapper modelMapper;

    @Override
    public WorkspaceMemberResponseDto mapTo(WorkspaceMember workspaceMember) {
        return modelMapper.map(workspaceMember, WorkspaceMemberResponseDto.class);
    }

    @Override
    public WorkspaceMember mapFrom(WorkspaceMemberResponseDto workspaceMemberResponseDto) {
        return modelMapper.map(workspaceMemberResponseDto, WorkspaceMember.class);
    }
}
