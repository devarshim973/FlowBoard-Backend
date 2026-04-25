package com.flowboard.auth_service.service;

import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.utils.CustomPageResponse;

import java.util.List;

public interface UserService {
    public UserDto getUserByEmail(String email);

    public UserDto getUserById(Integer id);

    public UserDto updateProfile(Integer id, UserUpdateDto userUpdateDto);

    public void deactivateAccount(Integer id);

    public String deleteById(Integer userId, Integer loggedUserId);

    public CustomPageResponse<UserDto> findAllByRole(String roleStr, int page, int size, String sortBy, String direction);

    public CustomPageResponse<UserDto> searchByFullName(String fullName, int page, int size, String sortBy, String direction);

    User findById(Integer userId);

    public UserDto updateAvatarUrl(Integer id, String url);

    public String getEmailById(Integer id);

    public List<Integer> findAllUserIdByEmail(List<String> userEmailList);

    public List<UserDto> getBulkUser(List<Integer> userIds);

    Boolean checkByUserId(Integer userId);

    CustomPageResponse<UserDto> findAll(int page, int size, String sortBy, String direction);

    void deleteUser(Integer userId);

    void disable(Integer userId);

    UserDto searchByEmail(String email);

    void enable(Integer userId);
}
