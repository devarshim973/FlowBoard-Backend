package com.flowboard.auth_service.service.impl;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.UserService;
import com.flowboard.auth_service.utils.CustomPageResponse;
import com.flowboard.auth_service.utils.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final Mapper<User, UserDto> userResponseMapper;
    private final SecurityUtils securityUtils;
    private final EmailService emailService;

    @Override
    public UserDto getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + email));
        return userResponseMapper.mapTo(user);
    }

    @Override
    public UserDto getUserById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        return userResponseMapper.mapTo(user);
    }

    @Override
    public UserDto updateProfile(Integer id, UserUpdateDto userUpdateDto) {
        String email = securityUtils.getLoggedInUserEmail();
        User loggedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + email));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));

        if(!loggedUser.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Same user must be logged in to delete");
        }

        user.setFullName(userUpdateDto.getFullName());
        user.setAvatarUrl(userUpdateDto.getAvatarUrl());
        User updatedUser = userRepository.save(user);

        return userResponseMapper.mapTo(updatedUser);
    }

    @Override
    public String deleteById(Integer userId, Integer loggedUserId) {
        if(!userId.equals(loggedUserId)) throw new UserNotFoundException("Same user must be logged in to delete");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));

        userRepository.delete(user);
        return "User deleted successfully";
    }

    @Override
    public void deactivateAccount(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        user.setActive(false);
    }

    @Override
    public CustomPageResponse<UserDto> findAllByRole(String roleStr, int page, int size, String sortBy, String direction) {
        ROLE role = ROLE.valueOf(roleStr);

        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(sortBy).ascending();
        else sort = Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        log.info(pageable.toString());

        Page<User> userResponsePage = userRepository.findAllByRole(role, pageable);

        Page<UserDto> userDtoCustomPageResponse = userResponsePage.map(userResponseMapper::mapTo);

        return new CustomPageResponse<>(userDtoCustomPageResponse);
    }

    @Override
    public CustomPageResponse<UserDto> searchByFullName(String fullName, int page, int size, String sortBy, String direction) {
        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(sortBy).ascending();
        else sort = Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userResponsePage = userRepository.searchByFullName(fullName, pageable);
        Page<UserDto> userDtoCustomPageResponse = userResponsePage.map(userResponseMapper::mapTo);

        return new CustomPageResponse<>(userDtoCustomPageResponse);
    }

    @Override
    public User findById(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + userId));
    }

    @Override
    public UserDto updateAvatarUrl(Integer id, String url) {
        String email = securityUtils.getLoggedInUserEmail();
        User loggedUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email " + email));

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));

        if(!loggedUser.getUserId().equals(user.getUserId())) {
            throw new IllegalArgumentException("Same user must be logged in to update");
        }

        user.setAvatarUrl(url);
        User savedUser = userRepository.save(user);
        return userResponseMapper.mapTo(savedUser);
    }

    @Override
    public CustomPageResponse<UserDto> findAll(int page, int size, String sortBy, String direction) {
        Sort sort;
        if(direction.equals("asc")) sort = Sort.by(sortBy).ascending();
        else sort = Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> userPage = userRepository.findAll(pageable);

        Page<UserDto> userDtoPage = userPage.map(userResponseMapper::mapTo);

        return new CustomPageResponse<>(userDtoPage);
    }

    @Override
    public void deleteUser(Integer userId) {
        userRepository.deleteById(userId);
    }

    @Override
    public void disable(Integer userId) {
        User user = getUser(userId);
        user.setActive(false);
        emailService.sendAccountDeactivatedMail(user.getEmail());
        userRepository.save(user);
    }

    @Override
    public UserDto searchByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
        return userResponseMapper.mapTo(user);
    }

    @Override
    public String getEmailById(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id " + id));
        return user.getEmail();
    }

    @Override
    public List<Integer> findAllUserIdByEmail(List<String> userEmailList) {
        log.info("Find user id called for " + userEmailList.toString());
        List<Integer> result = new ArrayList<>();
        userEmailList.stream()
                .forEach(email -> {
                    Optional<User> userOptional = userRepository.findByEmail(email);
                    if(userOptional.isPresent()) result.add(userOptional.get().getUserId());
                });
        log.info(result.toString());
        return result;
    }

    @Override
    public List<UserDto> getBulkUser(List<Integer> userIds) {
        List<User> users = userRepository.findAllByUserIdIn(userIds);
        return users
                .stream()
                .map(userResponseMapper::mapTo)
                .collect(Collectors.toList());
    }

    @Override
    public void enable(Integer userId) {
        User user = getUser(userId);
        user.setActive(true);
        emailService.sendAccountActivationMail(user.getEmail());
        userRepository.save(user);
    }

    @Override
    public Boolean checkByUserId(Integer userId) {
        return userRepository.findById(userId).isPresent();
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
