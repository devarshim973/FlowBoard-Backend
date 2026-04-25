package com.flowboard.auth_service.utils;

import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtils {
    public String getLoggedInUserEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
