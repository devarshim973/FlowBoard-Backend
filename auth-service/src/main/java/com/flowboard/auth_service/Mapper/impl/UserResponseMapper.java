package com.flowboard.auth_service.Mapper.impl;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserResponseMapper implements Mapper<User, UserDto> {
    private final ModelMapper modelMapper;
    @Override
    public UserDto mapTo(User user) {
        return modelMapper.map(user, UserDto.class);
    }

    @Override
    public User mapFrom(UserDto userDto) {
        return modelMapper.map(userDto, User.class);
    }
}
