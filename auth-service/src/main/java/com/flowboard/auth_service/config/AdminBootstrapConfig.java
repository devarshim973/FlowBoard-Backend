package com.flowboard.auth_service.config;

import com.flowboard.auth_service.entity.PROVIDER;
import com.flowboard.auth_service.entity.ROLE;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminBootstrapConfig implements CommandLineRunner {
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${ADMIN_EMAIL:devarshim973@gmail.com}")
    private String adminEmail;

    @Value("${ADMIN_PASSWORD:}")
    private String adminPassword;

    @Override
    public void run(String... args) {
        if (adminPassword == null || adminPassword.isBlank()) {
            log.warn("ADMIN_PASSWORD is not set. Skipping admin bootstrap for {}", adminEmail);
            return;
        }

        User adminUser = userRepository.findByEmail(adminEmail).orElseGet(User::new);
        boolean isNewUser = adminUser.getUserId() == null;

        if (isNewUser) {
            adminUser.setEmail(adminEmail);
            adminUser.setFullName("Platform Admin");
            adminUser.setProvider(PROVIDER.MANUAL);
            adminUser.setAvatarUrl("");
        }

        adminUser.setPassword(passwordEncoder.encode(adminPassword));
        adminUser.setRole(ROLE.PLATFORM_ADMIN);
        adminUser.setActive(true);

        userRepository.save(adminUser);
        log.info("Platform admin account is ready for {}", adminEmail);
    }
}
