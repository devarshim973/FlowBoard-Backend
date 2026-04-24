package com.flowboard.flowboard_api_gateway.filter;

import com.flowboard.flowboard_api_gateway.security.JwtUtil;
import com.flowboard.flowboard_api_gateway.security.RouteValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
@Slf4j
@RequiredArgsConstructor
public class AuthenticationFilter implements GlobalFilter, Ordered {
    private final RouteValidator routeValidator;
    private final JwtUtil jwtUtil;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        if (HttpMethod.OPTIONS.equals(exchange.getRequest().getMethod())) {
            return chain.filter(exchange);
        }

        if (!routeValidator.isSecured.test(path)) {
            return chain.filter(exchange);
        }

        String forwardedUserId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (forwardedUserId != null && !forwardedUserId.isBlank()) {
            ServerWebExchange passthroughExchange = exchange.mutate()
                    .request(builder -> builder
                            .header("X-User-Id", forwardedUserId)
                            .header("X-User-Role", exchange.getRequest().getHeaders().getFirst("X-User-Role") == null ? "MEMBER" : exchange.getRequest().getHeaders().getFirst("X-User-Role"))
                    )
                    .build();
            return chain.filter(passthroughExchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return onError(exchange, "Missing or invalid Authorization Header", HttpStatus.UNAUTHORIZED);
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            return onError(exchange, "Invalid Token", HttpStatus.UNAUTHORIZED);
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);
        Integer userId = jwtUtil.extractUserId(token);

        ServerWebExchange modifiedExchange = exchange.mutate()
                .request(builder -> builder
                        .header("X-User-Name", username)
                        .header("X-User-Role", role == null ? "MEMBER" : role)
                        .header("X-User-Id", String.valueOf(userId))
                )
                .build();

        return chain.filter(modifiedExchange);
    }

    private Mono<Void> onError(ServerWebExchange exchange, String err, HttpStatus status) {
        log.info(err);
        exchange.getResponse().setStatusCode(status);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }
}