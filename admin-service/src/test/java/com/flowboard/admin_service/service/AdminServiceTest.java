package com.flowboard.admin_service.service;

import com.flowboard.admin_service.client.BoardAdminClient;
import com.flowboard.admin_service.client.WorkspaceAdminClient;
import com.flowboard.admin_service.config.AdminProperties;
import com.flowboard.admin_service.dto.LoginDto;
import com.flowboard.admin_service.dto.UserDto;
import com.flowboard.admin_service.entity.PROVIDER;
import com.flowboard.admin_service.entity.ROLE;
import com.flowboard.admin_service.entity.User;
import com.flowboard.admin_service.repository.UserRepository;
import com.flowboard.admin_service.util.CustomPageResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminServiceTest {

    private AdminProperties adminProperties;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private WorkspaceAdminClient workspaceAdminClient;
    @Mock
    private BoardAdminClient boardAdminClient;

    private AdminService adminService;

    @BeforeEach
    void setUp() {
        adminProperties = new AdminProperties();
        adminService = new AdminService(
                adminProperties,
                passwordEncoder,
                jwtService,
                userRepository,
                workspaceAdminClient,
                boardAdminClient
        );
    }

    @Test
    void loginShouldReturnTokenForPersistedPlatformAdmin() {
        LoginDto loginDto = new LoginDto("admin@flowboard.com", "Admin@123");
        User adminUser = createUser(11, ROLE.PLATFORM_ADMIN, true);

        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(adminUser));
        when(passwordEncoder.matches(loginDto.getPassword(), adminUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(loginDto.getEmail(), ROLE.PLATFORM_ADMIN.name(), adminUser.getUserId())).thenReturn("jwt-token");

        String token = adminService.login(loginDto);

        assertEquals("jwt-token", token);
    }

    @Test
    void loginShouldUseConfiguredAdminWhenRepositoryUserCannotAuthenticate() {
        LoginDto loginDto = new LoginDto("owner@flowboard.com", "Admin@123");
        User inactiveAdmin = createUser(12, ROLE.PLATFORM_ADMIN, false);
        setConfiguredAdmin(loginDto.getEmail(), "Admin@123");

        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.of(inactiveAdmin));
        when(jwtService.generateToken(loginDto.getEmail(), "ADMIN", -1)).thenReturn("configured-token");

        String token = adminService.login(loginDto);

        assertEquals("configured-token", token);
    }

    @Test
    void loginShouldRejectUnknownConfiguredAdminEmail() {
        LoginDto loginDto = new LoginDto("owner@flowboard.com", "Admin@123");
        setConfiguredAdmin("other@flowboard.com", "Admin@123");
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> adminService.login(loginDto));

        assertEquals("Invalid admin credentials", exception.getMessage());
    }

    @Test
    void loginShouldRejectMissingConfiguredPassword() {
        LoginDto loginDto = new LoginDto("owner@flowboard.com", "Admin@123");
        setConfiguredAdmin(loginDto.getEmail(), "   ");
        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> adminService.login(loginDto));

        assertEquals("Admin password hash is missing in admin-service application.yml", exception.getMessage());
    }

    @Test
    void loginShouldSupportHashedConfiguredPassword() {
        LoginDto loginDto = new LoginDto("owner@flowboard.com", "Admin@123");
        String hash = "$2a$10$abcdefghijklmnopqrstuv";
        setConfiguredAdmin(loginDto.getEmail(), hash);

        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginDto.getPassword(), hash)).thenReturn(true);
        when(jwtService.generateToken(loginDto.getEmail(), "ADMIN", -1)).thenReturn("hashed-token");

        String token = adminService.login(loginDto);

        assertEquals("hashed-token", token);
    }

    @Test
    void loginShouldRejectConfiguredPasswordMismatch() {
        LoginDto loginDto = new LoginDto("owner@flowboard.com", "Admin@123");
        String hash = "$2b$10$abcdefghijklmnopqrstuv";
        setConfiguredAdmin(loginDto.getEmail(), hash);

        when(userRepository.findByEmail(loginDto.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.matches(loginDto.getPassword(), hash)).thenReturn(false);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> adminService.login(loginDto));

        assertEquals("Invalid admin credentials", exception.getMessage());
    }

    @Test
    void dashboardShouldExposeTotalUsers() {
        when(userRepository.count()).thenReturn(17L);

        Map<String, Object> dashboard = adminService.dashboard();

        assertEquals("Admin dashboard loaded", dashboard.get("message"));
        assertEquals(17L, dashboard.get("totalUsers"));
    }

    @Test
    void findAllUsersShouldMapEntitiesToDtos() {
        User user = createUser(31, ROLE.USER, true);
        Page<User> page = new PageImpl<>(List.of(user));
        doReturn(page).when(userRepository).findAll(any(Pageable.class));

        CustomPageResponse<UserDto> response = adminService.findAllUsers(0, 10, "email", "asc");

        assertEquals(1, response.getNumberOfElements());
        assertEquals("member@flowboard.com", response.getContent().getFirst().getEmail());
        assertTrue(response.isFirst());
        verify(userRepository).findAll(argThat((Pageable pageable) -> pageable.getSort().getOrderFor("email") != null
                && pageable.getSort().getOrderFor("email").isAscending()));
    }

    @Test
    void searchByFullNameShouldUseDescendingSort() {
        User user = createUser(32, ROLE.USER, true);
        Page<User> page = new PageImpl<>(List.of(user));
        when(userRepository.searchByFullName(eq("Member"), any(Pageable.class))).thenReturn(page);

        CustomPageResponse<UserDto> response = adminService.searchByFullName("Member", 1, 5, "fullName", "desc");

        assertEquals(1, response.getContent().size());
        verify(userRepository).searchByFullName(eq("Member"), argThat((Pageable pageable) -> pageable.getSort().getOrderFor("fullName") != null
                && pageable.getSort().getOrderFor("fullName").isDescending()));
    }

    @Test
    void searchByEmailShouldReturnMappedUser() {
        User user = createUser(33, ROLE.USER, true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        UserDto result = adminService.searchByEmail(user.getEmail());

        assertEquals(user.getUserId(), result.getUserId());
        assertEquals(ROLE.USER.name(), result.getRole());
    }

    @Test
    void searchByEmailShouldThrowWhenUserMissing() {
        when(userRepository.findByEmail("missing@flowboard.com")).thenReturn(Optional.empty());

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> adminService.searchByEmail("missing@flowboard.com"));

        assertEquals("User not found", exception.getMessage());
    }

    @Test
    void deleteUserShouldRejectPlatformAdmin() {
        User adminUser = createUser(44, ROLE.PLATFORM_ADMIN, true);
        when(userRepository.findById(adminUser.getUserId())).thenReturn(Optional.of(adminUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> adminService.deleteUser(adminUser.getUserId()));

        assertEquals("Platform admin account cannot be deleted", exception.getMessage());
    }

    @Test
    void deleteUserShouldDeleteRegularUser() {
        User user = createUser(45, ROLE.USER, true);
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        String message = adminService.deleteUser(user.getUserId());

        assertEquals("User deleted successfully", message);
        verify(userRepository).delete(user);
    }

    @Test
    void updateUserStatusShouldEnableUser() {
        User user = createUser(46, ROLE.USER, false);
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        String message = adminService.updateUserStatus(user.getUserId(), true);

        assertEquals("User enabled successfully", message);
        assertTrue(user.isActive());
        verify(userRepository).save(user);
    }

    @Test
    void updateUserStatusShouldDisableUser() {
        User user = createUser(47, ROLE.USER, true);
        when(userRepository.findById(user.getUserId())).thenReturn(Optional.of(user));

        String message = adminService.updateUserStatus(user.getUserId(), false);

        assertEquals("User disabled successfully", message);
        assertFalse(user.isActive());
    }

    @Test
    void adminEndpointsShouldDelegateToFeignClients() {
        when(workspaceAdminClient.getWorkspaces("ADMIN")).thenReturn(List.of("workspace-1"));
        when(workspaceAdminClient.deleteWorkspace("ADMIN", 9)).thenReturn("workspace deleted");
        when(boardAdminClient.getBoards("ADMIN")).thenReturn(List.of("board-1"));
        when(boardAdminClient.deleteBoard("ADMIN", 5)).thenReturn("board deleted");

        assertEquals(List.of("workspace-1"), adminService.getWorkspaces());
        assertEquals("workspace deleted", adminService.deleteWorkspace(9));
        assertEquals(List.of("board-1"), adminService.getBoards());
        assertEquals("board deleted", adminService.deleteBoard(5));
    }

    private void setConfiguredAdmin(String email, String password) {
        ReflectionTestUtils.setField(adminProperties, "email", email);
        ReflectionTestUtils.setField(adminProperties, "password", password);
    }

    private User createUser(Integer userId, ROLE role, boolean active) {
        User user = new User();
        user.setUserId(userId);
        user.setFullName("Member User");
        user.setEmail("member@flowboard.com");
        user.setPassword("encoded-password");
        user.setRole(role);
        user.setAvatarUrl("avatar.png");
        user.setProvider(PROVIDER.MANUAL);
        user.setActive(active);
        return user;
    }
}
