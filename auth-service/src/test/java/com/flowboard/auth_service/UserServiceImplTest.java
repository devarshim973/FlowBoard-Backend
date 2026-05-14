package com.flowboard.auth_service;

import com.flowboard.auth_service.Mapper.Mapper;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.EmailService;
import com.flowboard.auth_service.service.impl.UserServiceImpl;
import com.flowboard.auth_service.utils.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private Mapper<User, UserDto> userResponseMapper;

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private UserServiceImpl userService;

    private User user;
    private UserDto dto;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setUserId(1);
        user.setEmail("john@gmail.com");
        user.setFullName("John");
        user.setRole(ROLE.USER);
        user.setActive(true);

        dto = new UserDto();
        dto.setUserId(1);
        dto.setEmail("john@gmail.com");
        dto.setFullName("John");
    }

    @Test
    void getUserByEmail_withValidEmail_returnsUserDto() {

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

        assertFalse(result);
    }

    @Test
    void deactivateAccount_withValidUser_disablesUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.deactivateAccount(1);

        assertFalse(user.isActive());
    }

    @Test
    void deactivateAccount_withMissingUser_throwsException() {
        when(userRepository.findById(7)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.deactivateAccount(7));
    }

    @Test
    void findAllByRole_withAscendingSort_returnsPage() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAllByRole(eq(ROLE.USER), any())).thenReturn(page);
        when(userResponseMapper.mapTo(user)).thenReturn(dto);

        var response = userService.findAllByRole("USER", 0, 10, "email", "asc");

        assertEquals(1, response.getContent().size());
        assertEquals("john@gmail.com", response.getContent().getFirst().getEmail());
    }

    @Test
    void searchByFullName_withDescendingSort_returnsPage() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.searchByFullName(eq("John"), any())).thenReturn(page);
        when(userResponseMapper.mapTo(user)).thenReturn(dto);

        var response = userService.searchByFullName("John", 0, 10, "fullName", "desc");

        assertEquals(1, response.getContent().size());
    }

    @Test
    void findById_withValidId_returnsUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        User result = userService.findById(1);

        assertEquals(1, result.getUserId());
    }

    @Test
    void findById_withMissingId_throwsException() {
        when(userRepository.findById(2)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(2));
    }

    @Test
    void updateAvatarUrl_withDifferentUser_throwsException() {
        User loggedUser = new User();
        loggedUser.setUserId(1);
        User anotherUser = new User();
        anotherUser.setUserId(2);

        when(securityUtils.getLoggedInUserEmail()).thenReturn("john@gmail.com");
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(loggedUser));
        when(userRepository.findById(2)).thenReturn(Optional.of(anotherUser));

        assertThrows(IllegalArgumentException.class, () -> userService.updateAvatarUrl(2, "newUrl"));
    }

    @Test
    void findAll_withDescendingSort_returnsPage() {
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.findAll(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);
        when(userResponseMapper.mapTo(user)).thenReturn(dto);

        var response = userService.findAll(0, 10, "createdAt", "desc");

        assertEquals(1, response.getContent().size());
    }

    @Test
    void deleteUser_withPlatformAdmin_throwsException() {
        user.setRole(ROLE.PLATFORM_ADMIN);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        assertThrows(IllegalArgumentException.class, () -> userService.deleteUser(1));
    }

    @Test
    void deleteUser_withRegularUser_deletesUser() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));

        userService.deleteUser(1);

        verify(userRepository).delete(user);
    }

    @Test
    void disable_withValidUser_sendsEmailAndSaves() {
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        doNothing().when(emailService).sendAccountDeactivatedMail("john@gmail.com");

        userService.disable(1);

        verify(emailService).sendAccountDeactivatedMail("john@gmail.com");
        verify(userRepository).save(user);
    }

    @Test
    void searchByEmail_withMissingUser_throwsException() {
        when(userRepository.findByEmail("missing@gmail.com")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.searchByEmail("missing@gmail.com"));
    }

    @Test
    void searchByEmail_withExistingUser_returnsDto() {
        when(userRepository.findByEmail("john@gmail.com")).thenReturn(Optional.of(user));
        when(userResponseMapper.mapTo(user)).thenReturn(dto);

        UserDto result = userService.searchByEmail("john@gmail.com");

        assertEquals("john@gmail.com", result.getEmail());
    }

    @Test
    void getEmailById_withMissingId_throwsException() {
        when(userRepository.findById(5)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.getEmailById(5));
    }

    @Test
    void enable_withValidUser_sendsEmailAndSaves() {
        user.setActive(false);
        when(userRepository.findById(1)).thenReturn(Optional.of(user));
        doNothing().when(emailService).sendAccountActivationMail("john@gmail.com");

        userService.enable(1);

        assertEquals(true, user.isActive());
        verify(emailService).sendAccountActivationMail("john@gmail.com");
        verify(userRepository).save(user);
    }
}
