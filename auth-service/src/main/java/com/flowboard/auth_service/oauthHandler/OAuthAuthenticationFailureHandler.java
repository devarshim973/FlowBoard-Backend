package com.flowboard.auth_service.oauthHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OAuthAuthenticationFailureHandler implements AuthenticationFailureHandler {
    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException, ServletException {
        log.info("OAuth - Login with google failed");
        log.info(exception.getMessage());
        log.info(exception.getLocalizedMessage());
        String message = exception.getMessage() == null ? "Google login failed" : exception.getMessage();
        response.sendRedirect(frontendUrl + "/auth?oauthError=" + URLEncoder.encode(message, StandardCharsets.UTF_8));
    }
}
