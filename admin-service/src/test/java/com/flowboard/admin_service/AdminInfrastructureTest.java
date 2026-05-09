package com.flowboard.admin_service;

import com.flowboard.admin_service.config.AdminProperties;
import com.flowboard.admin_service.config.AppConfig;
import com.flowboard.admin_service.controller.AdminController;
import com.flowboard.admin_service.dto.LoginDto;
import com.flowboard.admin_service.dto.UserDto;
import com.flowboard.admin_service.exception.GlobalExceptionHandler;
import com.flowboard.admin_service.filter.HeaderAuthFilter;
import com.flowboard.admin_service.service.AdminService;
import com.flowboard.admin_service.util.CustomPageResponse;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.boot.SpringApplication;
import org.springframework.data.domain.PageImpl;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AdminInfrastructureTest {

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void mainShouldDelegateToSpringApplication() {
        try (MockedStatic<SpringApplication> springApplication = mockStatic(SpringApplication.class)) {
            AdminServiceApplication.main(new String[]{"--test"});

            springApplication.verify(() -> SpringApplication.run(AdminServiceApplication.class, new String[]{"--test"}));
        }
    }

    @Test
    void appConfigShouldCreatePasswordEncoder() {
        AppConfig appConfig = new AppConfig();

        assertTrue(appConfig.bCryptPasswordEncoder().matches("secret", appConfig.bCryptPasswordEncoder().encode("secret")));
    }

    @Test
    void adminPropertiesShouldStoreValues() {
        AdminProperties properties = new AdminProperties();

        properties.setEmail("admin@flowboard.com");
        properties.setPassword("hashed-password");

        assertEquals("admin@flowboard.com", properties.getEmail());
        assertEquals("hashed-password", properties.getPassword());
    }

    @Test
    void headerAuthFilterShouldPopulateSecurityContextWhenHeaderPresent() throws ServletException, IOException {
        HeaderAuthFilter filter = new HeaderAuthFilter();
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader("X-User-Role", "ADMIN");

        filter.doFilter(request, new MockHttpServletResponse(), new MockFilterChain());

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals("admin-user", authentication.getPrincipal());
        assertTrue(authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void headerAuthFilterShouldLeaveSecurityContextEmptyWhenHeaderMissing() throws ServletException, IOException {
        HeaderAuthFilter filter = new HeaderAuthFilter();

        filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(), new MockFilterChain());

        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void globalExceptionHandlerShouldReturnBadRequestMessage() {
        GlobalExceptionHandler handler = new GlobalExceptionHandler();

        var response = handler.globalExceptionHandler(new IllegalArgumentException("boom"));

        assertEquals(400, response.getStatusCode().value());
        assertEquals("boom", response.getBody());
    }

    @Test
    void adminControllerShouldDelegateAllEndpoints() {
        AdminService adminService = mock(AdminService.class);
        AdminController controller = new AdminController(adminService);

        LoginDto loginDto = new LoginDto("admin@flowboard.com", "secret");
        UserDto userDto = new UserDto("Member", "member@flowboard.com", "avatar.png", 9, true, "USER");
        CustomPageResponse<UserDto> pageResponse = new CustomPageResponse<>(new PageImpl<>(List.of(userDto)));

        when(adminService.login(loginDto)).thenReturn("jwt");
        when(adminService.dashboard()).thenReturn(Map.of("totalUsers", 1L));
        when(adminService.findAllUsers(0, 10, "createdAt", "desc")).thenReturn(pageResponse);
        when(adminService.searchByFullName("Member", 0, 10, "createdAt", "desc")).thenReturn(pageResponse);
        when(adminService.searchByEmail("member@flowboard.com")).thenReturn(userDto);
        when(adminService.updateUserStatus(7, true)).thenReturn("enabled");
        when(adminService.updateUserStatus(7, false)).thenReturn("disabled");
        when(adminService.deleteUser(7)).thenReturn("deleted");
        when(adminService.getWorkspaces()).thenReturn(List.of("workspace-1"));
        when(adminService.deleteWorkspace(4)).thenReturn("workspace deleted");
        when(adminService.getBoards()).thenReturn(List.of("board-1"));
        when(adminService.deleteBoard(5)).thenReturn("board deleted");

        assertEquals("jwt", controller.login(loginDto).getBody());
        assertEquals(200, controller.dashboard().getStatusCode().value());
        assertEquals(pageResponse, controller.getUsers("createdAt", "desc", 0, 10).getBody());
        assertEquals(pageResponse, controller.getByFullName("Member", "createdAt", "desc", 0, 10).getBody());
        assertEquals(userDto, controller.getByEmail("member@flowboard.com").getBody());
        assertEquals("enabled", controller.updateUserStatus(7, true).getBody());
        assertEquals("disabled", controller.disableUser(7).getBody());
        assertEquals("enabled", controller.enableUser(7).getBody());
        assertEquals("deleted", controller.deleteUser(7).getBody());
        assertEquals(List.of("workspace-1"), controller.getWorkspaces().getBody());
        assertEquals("workspace deleted", controller.deleteWorkspace(4).getBody());
        assertEquals(List.of("board-1"), controller.getBoards().getBody());
        assertEquals("board deleted", controller.deleteBoard(5).getBody());

        verify(adminService).findAllUsers(0, 10, "createdAt", "desc");
        verify(adminService).searchByFullName("Member", 0, 10, "createdAt", "desc");
    }
}
