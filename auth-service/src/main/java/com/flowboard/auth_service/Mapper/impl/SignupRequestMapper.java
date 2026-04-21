package com.flowboard.auth_service.Mapper.impl;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.SignupDto;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.entity.User;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignupRequestMapper implements Mapper<SignupDto, User> {
    private final ModelMapper modelMapper;
    @Override
    public User mapTo(SignupDto signupDto) {
        return modelMapper.map(signupDto, User.class);
    }

    @Override
    public SignupDto mapFrom(User user) {
        return modelMapper.map(user, SignupDto.class);
    }
}
