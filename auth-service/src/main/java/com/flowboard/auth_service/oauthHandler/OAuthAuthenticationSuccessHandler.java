package com.flowboard.auth_service.oauthHandler;

import com.flowboard.auth_service.entity.PROVIDER;
import com.flowboard.auth_service.entity.User;
import com.flowboard.auth_service.exception.UserNotFoundException;
import com.flowboard.auth_service.repository.UserRepository;
import com.flowboard.auth_service.service.JwtService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OAuthAuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {
    private final JwtService jwtService;
    private final UserRepository userRepo;
    private final UserDetailsService userDetailsService;
    private final BCryptPasswordEncoder passwordEncoder;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {
        OAuth2AuthenticationToken auth2AuthenticationToken = (OAuth2AuthenticationToken)  authentication;
        OAuth2User oAuth2User = auth2AuthenticationToken.getPrincipal();

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("picture");

        log.info("OAuth google login successful " + email + " " + name);

        Optional<User> userOptional = userRepo.findByEmail(email);

        Integer userId = null;
        if(userOptional.isEmpty()) {
            User user = new User();
            user.setEmail(email);
            user.setFullName(name);
            user.setProvider(PROVIDER.GOOGLE);
            user.setAvatarUrl(avatarUrl);
            user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
            userRepo.save(user);
            userId = user.getUserId();
        }
        else {
            if(!userOptional.get().isActive()) {
                throw  new UserNotFoundException("User is disabled");
            }
            userId = userOptional.get().getUserId();
        }

        UserDetails userDetails = userDetailsService.loadUserByUsername(email);
        String token = jwtService.generateToken(email, "USER", userId);

        String redirectUrl = "http://localhost:4200/oauth-success?token=" + token;

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

