package com.flowboard.auth_service.service;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.utils.CustomPageResponse;

public interface UserService {
    public UserDto getUserByEmail(String email);

    public UserDto getUserById(Integer id);

    public UserDto updateProfile(Integer id, UserUpdateDto userUpdateDto);

    public void deactivateAccount(Integer id);

    public String deleteById(Integer userId);
    public CustomPageResponse<UserDto> findAllByRole(String roleStr, int page, int size, String sortBy, String direction);

    public CustomPageResponse<UserDto> searchByFullName(String fullName, int page, int size, String sortBy, String direction);

    User findById(Integer userId);

    UserDto updateAvatarUrl(Integer id, String url);
}
