package com.flowboard.flowboard_api_gateway.filter;

import com.flowboard.flowboard_api_gateway.security.JwtUtil;
import com.flowboard.flowboard_api_gateway.security.RouteValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

@Component
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private static final Logger log = LoggerFactory.getLogger(AuthenticationFilter.class);
    private static final Set<String> ALLOWED_ORIGINS = Set.of(
            "http://localhost:5173",
            "http://localhost:4200",
            "https://flow-board-frontend-sage.vercel.app",
            "https://flowboard-application.duckdns.org"
    );

    private final RouteValidator routeValidator;
    private final JwtUtil jwtUtil;

    public AuthenticationFilter(RouteValidator routeValidator, JwtUtil jwtUtil) {
        this.routeValidator = routeValidator;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        log.info("Filter running");

        HttpMethod method = exchange.getRequest().getMethod();

        if (method == HttpMethod.OPTIONS) {
            log.info("Preflight request - returning gateway CORS response");
            return handlePreflight(exchange);
        }

        String path = exchange.getRequest().getURI().getPath();

        if (!routeValidator.isSecured.test(path)) {
            log.info("public url, directly calling the downstream service");
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || authHeader.isBlank()) {
            return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        if (!authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);

        if (!jwtUtil.isTokenValid(token)) {
            return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
        }


        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        Integer userId = jwtUtil.extractUserId(token);

        // Add headers to downstream services
        /*
        Yes the user can pass the header here if the user tries to send any other user's
        id then jwt will override it with the user id which is in payload of jwt token
        and if the token is wrong then user will be not allowed
         */
        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(builder -> builder
                        .header("X-User-Name", username)
                        .header("X-User-Role", role)
                        .header("X-User-Id", String.valueOf(userId))
                )
                .build();

        log.info("Token validation successful");
        return chain.filter(modifiedExchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        log.info("Invalid token");
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> handlePreflight(ServerWebExchange exchange) {
        String origin = exchange.getRequest().getHeaders().getOrigin();

        if (origin != null && ALLOWED_ORIGINS.contains(origin)) {
            exchange.getResponse().getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, origin);
            exchange.getResponse().getHeaders().set(HttpHeaders.VARY, "Origin");
            exchange.getResponse().getHeaders().set(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true");
            exchange.getResponse().getHeaders().set(
                    HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                    "GET,POST,PUT,PATCH,DELETE,OPTIONS"
            );

            String requestedHeaders = exchange.getRequest()
                    .getHeaders()
                    .getFirst(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS);

            exchange.getResponse().getHeaders().set(
                    HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS,
                    requestedHeaders == null || requestedHeaders.isBlank()
                            ? "Authorization,Content-Type,X-User-Id,X-User-Role,X-User-Name"
                            : requestedHeaders
            );
        }

        exchange.getResponse().setStatusCode(HttpStatus.OK);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}
