package com.flowboard.list_service.mapper.impl;

import com.flowboard.list_service.dto.TaskListRequestDto;
import com.flowboard.list_service.entity.TaskList;
import com.flowboard.list_service.mapper.Mapper;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskListRequestMapper implements Mapper<TaskListRequestDto, TaskList> {
    private final ModelMapper modelMapper;
    @Override
    public TaskList mapTo(TaskListRequestDto taskListRequestDto) {
        return modelMapper.map(taskListRequestDto, TaskList.class);
    }

    @Override
    public TaskListRequestDto mapFrom(TaskList taskList) {
        return modelMapper.map(taskList, TaskListRequestDto.class);
    }
}
