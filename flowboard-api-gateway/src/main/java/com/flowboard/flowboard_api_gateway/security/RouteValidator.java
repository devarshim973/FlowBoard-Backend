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
            "/api/v1/auth/forget",
            "/api/v1/workspaces/public",
            "/api/v1/boards/get/workspace/",
            "/api/v1/lists/public",
            "/oauth2/authorization/google",
            "/api/v1/subscriptions/details",
            "/login/oauth2/code/google",
            "/api/v1/auth/register-admin"
    );
    public Predicate<String> isSecured =
            uri -> {
                for(String endpoint : openApiEndpoints) {
                    if(uri.startsWith(endpoint)) return false;
                }
                return true;
            };
}