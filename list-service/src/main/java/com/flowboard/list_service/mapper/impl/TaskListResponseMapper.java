package com.flowboard.list_service.mapper.impl;

import com.flowboard.list_service.dto.TaskListResponseDto;
import com.flowboard.list_service.entity.TaskList;
import com.flowboard.list_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskListResponseMapper implements Mapper<TaskList, TaskListResponseDto> {
    private final ModelMapper modelMapper;

    @Override
    public TaskListResponseDto mapTo(TaskList taskList) {
        return modelMapper.map(taskList, TaskListResponseDto.class);
    }

    @Override
    public TaskList mapFrom(TaskListResponseDto taskListResponseDto) {
        return modelMapper.map(taskListResponseDto, TaskList.class);
    }
}
