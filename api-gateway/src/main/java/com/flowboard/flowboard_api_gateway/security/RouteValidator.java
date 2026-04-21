package com.flowboard.flowboard_api_gateway.security;

import org.springframework.stereotype.Component;
import java.util.List;
import java.util.function.Predicate;

@Component
public class RouteValidator {
    private static final List<String> openApiEndpoints = List.of(
            "/api/v1/auth/signup",
            "/api/v1/auth/login",
            "/api/v1/auth/verify",
            "/api/v1/auth/sendotp",
            "/api/v1/auth/forget"
    );
    public Predicate<String> isSecured =
            uri -> openApiEndpoints.stream()
                    .noneMatch(uri::startsWith);
}