package com.flowboard.auth_service;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.impl.UserServiceImpl;
import com.flowboard.auth_service.utils.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.lenient;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Mapper<User, UserDto> userResponseMapper;

    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void getUserByEmail_withValidEmail_returnsUserDto() {

        User user = new User();
        user.setEmail("john@gmail.com");

        UserDto dto = new UserDto();
        dto.setEmail("john@gmail.com");

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(user));

        when(userResponseMapper.mapTo(user))
                .thenReturn(dto);

        UserDto result = userService.getUserByEmail("john@gmail.com");

        assertEquals("john@gmail.com", result.getEmail());
    }

    @Test
    void getUserByEmail_withWrongEmail_throwsException() {

        when(userRepository.findByEmail("wrong@gmail.com"))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserByEmail("wrong@gmail.com"));
    }

    @Test
    void getUserById_withValidId_returnsUserDto() {

        User user = new User();
        user.setUserId(1);

        UserDto dto = new UserDto();
        dto.setUserId(1);

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(userResponseMapper.mapTo(user))
                .thenReturn(dto);

        UserDto result = userService.getUserById(1);

        assertEquals(1, result.getUserId());
    }

    @Test
    void getUserById_withWrongId_throwsException() {

        when(userRepository.findById(99))
                .thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class,
                () -> userService.getUserById(99));
    }

    @Test
    void updateProfile_withValidUser_returnsUpdatedDto() {

        User loggedUser = new User();
        loggedUser.setUserId(1);

        User user = new User();
        user.setUserId(1);

        UserDto dto = new UserDto();
        dto.setFullName("Updated");

        UserUpdateDto request =
                new UserUpdateDto("Updated", "url");

        when(securityUtils.getLoggedInUserEmail())
                .thenReturn("john@gmail.com");

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(loggedUser));

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userResponseMapper.mapTo(user))
                .thenReturn(dto);

        UserDto result = userService.updateProfile(1, request);

        assertEquals("Updated", result.getFullName());
    }

    @Test
    void updateProfile_withDifferentUser_throwsException() {

        User loggedUser = new User();
        loggedUser.setUserId(1);

        User user = new User();
        user.setUserId(2);

        when(securityUtils.getLoggedInUserEmail())
                .thenReturn("john@gmail.com");

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(loggedUser));

        when(userRepository.findById(2))
                .thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class,
                () -> userService.updateProfile(2,
                        new UserUpdateDto("A", "B")));
    }

    @Test
    void deleteById_withValidIds_returnsSuccess() {

        User user = new User();

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        String result = userService.deleteById(1, 1);

        assertEquals("User deleted successfully", result);
        verify(userRepository).delete(user);
    }

    @Test
    void deleteById_withDifferentIds_throwsException() {

        assertThrows(UserNotFoundException.class,
                () -> userService.deleteById(1, 2));
    }

    @Test
    void updateAvatarUrl_withValidUser_returnsDto() {

        User loggedUser = new User();
        loggedUser.setUserId(1);

        User user = new User();
        user.setUserId(1);

        UserDto dto = new UserDto();
        dto.setAvatarUrl("newUrl");

        when(securityUtils.getLoggedInUserEmail())
                .thenReturn("john@gmail.com");

        when(userRepository.findByEmail("john@gmail.com"))
                .thenReturn(Optional.of(loggedUser));

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        when(userRepository.save(user))
                .thenReturn(user);

        when(userResponseMapper.mapTo(user))
                .thenReturn(dto);

        UserDto result =
                userService.updateAvatarUrl(1, "newUrl");

        assertEquals("newUrl", result.getAvatarUrl());
    }

    @Test
    void getEmailById_withValidId_returnsEmail() {

        User user = new User();
        user.setEmail("john@gmail.com");

        when(userRepository.findById(1))
                .thenReturn(Optional.of(user));

        String result = userService.getEmailById(1);

        assertEquals("john@gmail.com", result);
    }

    @Test
    void findAllUserIdByEmail_returnsIds() {

        User user1 = new User();
        user1.setUserId(1);

        User user2 = new User();
        user2.setUserId(2);

        when(userRepository.findByEmail("a@gmail.com"))
                .thenReturn(Optional.of(user1));

        when(userRepository.findByEmail("b@gmail.com"))
                .thenReturn(Optional.of(user2));

        List<Integer> result =
                userService.findAllUserIdByEmail(
                        List.of("a@gmail.com", "b@gmail.com"));

        assertEquals(2, result.size());
    }

    @Test
    void getBulkUser_returnsList() {

        User user = new User();
        user.setUserId(1);

        UserDto dto = new UserDto();
        dto.setUserId(1);

        when(userRepository.findAllByUserIdIn(any()))
                .thenReturn(List.of(user));

        when(userResponseMapper.mapTo(user))
                .thenReturn(dto);

        List<UserDto> result =
                userService.getBulkUser(List.of(1));

        assertEquals(1, result.size());
    }

    @Test
    void checkByUserId_withValidId_returnsTrue() {

        when(userRepository.findById(1))
                .thenReturn(Optional.of(new User()));

        Boolean result = userService.checkByUserId(1);

        assertEquals(true, result);
    }

    @Test
    void checkByUserId_withWrongId_returnsFalse() {

        when(userRepository.findById(99))
                .thenReturn(Optional.empty());

        Boolean result = userService.checkByUserId(99);

        assertEquals(false, result);
    }
}