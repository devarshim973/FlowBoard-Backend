package com.flowboard.auth_service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.flowboard.auth_service.controller.UserController;
import com.flowboard.auth_service.dto.UserDto;
import com.flowboard.auth_service.dto.UserUpdateDto;
import com.flowboard.auth_service.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.servlet.OAuth2ClientAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        value = UserController.class,
        excludeAutoConfiguration = {
                OAuth2ClientAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void getUserByEmail_withValidEmail_returns200() throws Exception {

        UserDto dto = new UserDto("John", "john@gmail.com", "url", 1, false, null);

        when(userService.getUserByEmail("john@gmail.com")).thenReturn(dto);

        mockMvc.perform(get("/api/v1/user/user-email/john@gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@gmail.com"));
    }

    @Test
    void getUserByEmail_withWrongEmail_returns400() throws Exception {

        when(userService.getUserByEmail("ghost@gmail.com"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/v1/user/user-email/ghost@gmail.com"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getUserById_withValidId_returns200() throws Exception {

        UserDto dto = new UserDto("John", "john@gmail.com", "url", 1, false, null);

        when(userService.getUserById(1)).thenReturn(dto);

        mockMvc.perform(get("/api/v1/user/id/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(1));
    }

    @Test
    void getUserById_withWrongId_returns400() throws Exception {

        when(userService.getUserById(99))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/v1/user/id/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteUser_withValidId_returns200() throws Exception {

        when(userService.deleteById(1, 1))
                .thenReturn("User deleted successfully");

        mockMvc.perform(delete("/api/v1/user/delete/1")
                        .header("X-User-Id", 1))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));
    }

    @Test
    void deleteUser_withWrongId_returns400() throws Exception {

        when(userService.deleteById(99, 1))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(delete("/api/v1/user/delete/99")
                        .header("X-User-Id", 1))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateProfile_withValidData_returns202() throws Exception {

        UserUpdateDto request = new UserUpdateDto("Updated John", "newUrl");
        UserDto response = new UserDto("Updated John", "john@gmail.com", "newUrl", 1, false, null);

        when(userService.updateProfile(any(), any(UserUpdateDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/api/v1/user/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.fullName").value("Updated John"));
    }

    @Test
    void updateProfile_withInvalidData_returns400() throws Exception {

        UserUpdateDto request = new UserUpdateDto("", "url");

        mockMvc.perform(put("/api/v1/user/update/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateAvatar_withValidData_returns202() throws Exception {

        UserDto dto = new UserDto("John", "john@gmail.com", "newUrl", 1, false, null);

        when(userService.updateAvatarUrl(1, "newUrl")).thenReturn(dto);

        mockMvc.perform(patch("/api/v1/user/avtarurl/1/newUrl"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.avatarUrl").value("newUrl"));
    }

    @Test
    void updateAvatar_withWrongId_returns400() throws Exception {

        when(userService.updateAvatarUrl(99, "newUrl"))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(patch("/api/v1/user/avtarurl/99/newUrl"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getBulkUsers_withValidIds_returns200() throws Exception {

        List<UserDto> users = List.of(
                new UserDto("John", "john@gmail.com", "url", 1, false, null),
                new UserDto("Sam", "sam@gmail.com", "url", 2, false, null)
        );

        when(userService.getBulkUser(any())).thenReturn(users);

        mockMvc.perform(get("/api/v1/user/bulk")
                        .param("userIds", "1", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value(1));
    }

    @Test
    void getUserEmail_withValidId_returns200() throws Exception {

        when(userService.getEmailById(1))
                .thenReturn("john@gmail.com");

        mockMvc.perform(get("/api/v1/user/email/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("john@gmail.com"));
    }

    @Test
    void getUserEmail_withWrongId_returns400() throws Exception {

        when(userService.getEmailById(99))
                .thenThrow(new RuntimeException("User not found"));

        mockMvc.perform(get("/api/v1/user/email/99"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void checkUser_withValidId_returns200() throws Exception {

        when(userService.checkByUserId(1)).thenReturn(true);

        mockMvc.perform(get("/api/v1/user/check/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void checkUser_withWrongId_returns200() throws Exception {

        when(userService.checkByUserId(99)).thenReturn(false);

        mockMvc.perform(get("/api/v1/user/check/99"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
