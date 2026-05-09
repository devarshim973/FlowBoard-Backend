package com.flowboard.auth_service;

import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.impl.UserDetailsServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void loadUserByUsername_withExistingUser_returnsUser() {
        User user = new User();
        user.setEmail("user@flowboard.com");
        user.setPassword("Password@1");
        user.setRole(ROLE.USER);
        when(userRepository.findByEmail("user@flowboard.com")).thenReturn(Optional.of(user));

        UserDetails result = userDetailsService.loadUserByUsername("user@flowboard.com");

        assertEquals("user@flowboard.com", result.getUsername());
    }

    @Test
    void loadUserByUsername_withMissingUser_throwsException() {
        when(userRepository.findByEmail("missing@flowboard.com")).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class,
                () -> userDetailsService.loadUserByUsername("missing@flowboard.com"));
    }
}
