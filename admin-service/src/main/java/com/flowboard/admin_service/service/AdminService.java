package com.flowboard.admin_service.service;

import com.flowboard.admin_service.client.BoardAdminClient;
import com.flowboard.admin_service.client.WorkspaceAdminClient;
import com.flowboard.admin_service.config.AdminProperties;
import com.flowboard.admin_service.dto.LoginDto;
import com.flowboard.admin_service.dto.UserDto;
import com.flowboard.admin_service.entity.ROLE;
import com.flowboard.admin_service.entity.User;
import com.flowboard.admin_service.repository.UserRepository;
import com.flowboard.admin_service.util.CustomPageResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminService {
    private static final Logger adminActivityLogger = LoggerFactory.getLogger("ADMIN_ACTIVITY");
    private static final Integer CONFIG_ADMIN_USER_ID = -1;

    private final AdminProperties adminProperties;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final WorkspaceAdminClient workspaceAdminClient;
    private final BoardAdminClient boardAdminClient;

    public String login(LoginDto loginDto) {
        User adminUser = userRepository.findByEmail(loginDto.getEmail())
                .filter(user -> user.getRole() == ROLE.PLATFORM_ADMIN)
                .orElse(null);

        if (adminUser != null) {
            if (adminUser.isActive() && passwordEncoder.matches(loginDto.getPassword(), adminUser.getPassword())) {
                adminActivityLogger.info("Platform admin login successful for {}", loginDto.getEmail());
                return jwtService.generateToken(loginDto.getEmail(), adminUser.getRole().name(), adminUser.getUserId());
            }
        }

        if (adminProperties.getEmail() == null || !adminProperties.getEmail().equalsIgnoreCase(loginDto.getEmail())) {
            throw new IllegalArgumentException("Invalid admin credentials");
        }
        if (adminProperties.getPassword() == null || adminProperties.getPassword().isBlank()) {
            throw new IllegalArgumentException("Admin password hash is missing in admin-service application.yml");
        }
        String configuredPassword = adminProperties.getPassword().trim();
        boolean passwordMatches = configuredPassword.startsWith("$2a$")
                || configuredPassword.startsWith("$2b$")
                || configuredPassword.startsWith("$2y$")
                ? passwordEncoder.matches(loginDto.getPassword(), configuredPassword)
                : configuredPassword.equals(loginDto.getPassword());

        if (!passwordMatches) {
            throw new IllegalArgumentException("Invalid admin credentials");
        }
        adminActivityLogger.info("Admin login successful for {}", loginDto.getEmail());
        return jwtService.generateToken(loginDto.getEmail(), "ADMIN", CONFIG_ADMIN_USER_ID);
    }

    public Map<String, Object> dashboard() {
        adminActivityLogger.info("Admin dashboard viewed");
        return Map.of(
                "message", "Admin dashboard loaded",
                "totalUsers", userRepository.count()
        );
    }

    public CustomPageResponse<UserDto> findAllUsers(int page, int size, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, direction));
        Page<UserDto> userPage = userRepository.findAll(pageable).map(this::toDto);
        adminActivityLogger.info("Admin requested user list page {} size {}", page, size);
        return new CustomPageResponse<>(userPage);
    }

    public CustomPageResponse<UserDto> searchByFullName(String fullName, int page, int size, String sortBy, String direction) {
        Pageable pageable = PageRequest.of(page, size, buildSort(sortBy, direction));
        Page<UserDto> userPage = userRepository.searchByFullName(fullName, pageable).map(this::toDto);
        adminActivityLogger.info("Admin searched users by full name {}", fullName);
        return new CustomPageResponse<>(userPage);
    }

    public UserDto searchByEmail(String email) {
        adminActivityLogger.info("Admin searched user by email {}", email);
        return toDto(userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("User not found")));
    }

    public String deleteUser(Integer userId) {
        User user = getUser(userId);
        if (user.getRole() == ROLE.PLATFORM_ADMIN) {
            throw new IllegalArgumentException("Platform admin account cannot be deleted");
        }
        userRepository.delete(user);
        adminActivityLogger.info("Admin deleted user {}", userId);
        return "User deleted successfully";
    }

    public String updateUserStatus(Integer userId, boolean active) {
        User user = getUser(userId);
        user.setActive(active);
        userRepository.save(user);
        adminActivityLogger.info("Admin updated user {} status to {}", userId, active);
        return active ? "User enabled successfully" : "User disabled successfully";
    }

    public Object getWorkspaces() {
        adminActivityLogger.info("Admin requested workspace list");
        return workspaceAdminClient.getWorkspaces("ADMIN");
    }

    public String deleteWorkspace(Integer workspaceId) {
        adminActivityLogger.info("Admin deleted workspace {}", workspaceId);
        return workspaceAdminClient.deleteWorkspace("ADMIN", workspaceId);
    }

    public Object getBoards() {
        adminActivityLogger.info("Admin requested board list");
        return boardAdminClient.getBoards("ADMIN");
    }

    public String deleteBoard(Integer boardId) {
        adminActivityLogger.info("Admin deleted board {}", boardId);
        return boardAdminClient.deleteBoard("ADMIN", boardId);
    }

    private User getUser(Integer userId) {
        return userRepository.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private UserDto toDto(User user) {
        return new UserDto(
                user.getFullName(),
                user.getEmail(),
                user.getAvatarUrl(),
                user.getUserId(),
                user.isActive(),
                user.getRole().name()
        );
    }

    private Sort buildSort(String sortBy, String direction) {
        return "asc".equalsIgnoreCase(direction) ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
    }
}
