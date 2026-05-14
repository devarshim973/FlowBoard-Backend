package com.flowboard.flowboard_api_gateway;

import com.flowboard.flowboard_api_gateway.config.RedisConfig;
import com.flowboard.flowboard_api_gateway.filter.AuthenticationFilter;
import com.flowboard.flowboard_api_gateway.filter.RateLimitFilter;
import com.flowboard.flowboard_api_gateway.security.JwtUtil;
import com.flowboard.flowboard_api_gateway.security.RouteValidator;
import com.flowboard.flowboard_api_gateway.security.CorsConfig;
import com.flowboard.flowboard_api_gateway.service.RateLimiterService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Encoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.Test;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.cors.reactive.CorsWebFilter;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.net.InetSocketAddress;
import java.time.Instant;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class GatewayInfrastructureTest {

    @Test
    void routeValidatorShouldAllowConfiguredPublicPaths() {
        RouteValidator validator = new RouteValidator();

        assertFalse(validator.isSecured.test("/api/v1/auth/login"));
        assertTrue(validator.isSecured.test("/api/v1/cards/1"));
    }

    @Test
    void jwtUtilShouldExtractClaimsAndValidateExpiration() {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String secret = Encoders.BASE64.encode(key.getEncoded());
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);

        String validToken = Jwts.builder()
                .subject("member@flowboard.com")
                .claim("role", "ADMIN")
                .claim("userId", 7)
                .expiration(Date.from(Instant.now().plusSeconds(3600)))
                .signWith(key)
                .compact();

        String expiredToken = Jwts.builder()
                .subject("member@flowboard.com")
                .claim("role", "ADMIN")
                .claim("userId", 7)
                .expiration(Date.from(Instant.now().minusSeconds(60)))
                .signWith(key)
                .compact();

        assertEquals("member@flowboard.com", jwtUtil.extractUsername(validToken));
        assertEquals("ADMIN", jwtUtil.extractRole(validToken));
        assertEquals(7, jwtUtil.extractUserId(validToken));
        assertTrue(jwtUtil.isTokenValid(validToken));
        assertFalse(jwtUtil.isTokenValid(expiredToken));
    }

    @Test
    void authenticationFilterShouldBypassOptionsAndPublicRoutes() {
        RouteValidator validator = new RouteValidator();
        validator.isSecured = path -> !path.startsWith("/public");
        JwtUtil jwtUtil = createJwtUtil();
        AuthenticationFilter filter = new AuthenticationFilter(validator, jwtUtil);
        AtomicBoolean invoked = new AtomicBoolean(false);
        GatewayFilterChain chain = exchange -> {
            invoked.set(true);
            return Mono.empty();
        };

        MockServerWebExchange optionsExchange = MockServerWebExchange.from(
                MockServerHttpRequest.method(HttpMethod.OPTIONS, "/secured").build()
        );
        MockServerWebExchange publicExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/public/hello").build()
        );

        filter.filter(optionsExchange, chain).block();
        filter.filter(publicExchange, chain).block();

        assertTrue(invoked.get());
    }

    @Test
    void authenticationFilterShouldRejectMissingInvalidAndBadTokens() {
        RouteValidator validator = new RouteValidator();
        validator.isSecured = path -> true;
        JwtUtil jwtUtil = createJwtUtil();
        AuthenticationFilter filter = new AuthenticationFilter(validator, jwtUtil);
        AtomicBoolean invoked = new AtomicBoolean(false);
        GatewayFilterChain chain = exchange -> {
            invoked.set(true);
            return Mono.empty();
        };

        MockServerWebExchange missingHeaderExchange = MockServerWebExchange.from(MockServerHttpRequest.get("/secured").build());
        filter.filter(missingHeaderExchange, chain).block();
        assertEquals(HttpStatus.UNAUTHORIZED, missingHeaderExchange.getResponse().getStatusCode());

        MockServerWebExchange invalidHeaderExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secured").header(HttpHeaders.AUTHORIZATION, "Token abc").build()
        );
        filter.filter(invalidHeaderExchange, chain).block();
        assertEquals(HttpStatus.UNAUTHORIZED, invalidHeaderExchange.getResponse().getStatusCode());

        MockServerWebExchange invalidTokenExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secured").header(HttpHeaders.AUTHORIZATION, "Bearer bad-token").build()
        );
        filter.filter(invalidTokenExchange, chain).block();
        assertEquals(HttpStatus.UNAUTHORIZED, invalidTokenExchange.getResponse().getStatusCode());

        assertFalse(invoked.get());
    }

    @Test
    void authenticationFilterShouldForwardUserHeadersForValidToken() {
        RouteValidator validator = new RouteValidator();
        validator.isSecured = path -> true;
        JwtUtil jwtUtil = createJwtUtil();
        AuthenticationFilter filter = new AuthenticationFilter(validator, jwtUtil);
        AtomicBoolean invoked = new AtomicBoolean(false);
        GatewayFilterChain chain = exchange -> {
            invoked.set(true);
            assertEquals("member@flowboard.com", exchange.getRequest().getHeaders().getFirst("X-User-Name"));
            assertEquals("USER", exchange.getRequest().getHeaders().getFirst("X-User-Role"));
            assertEquals("42", exchange.getRequest().getHeaders().getFirst("X-User-Id"));
            return Mono.empty();
        };

        String token = createToken(jwtUtil, "member@flowboard.com", "USER", 42, Instant.now().plusSeconds(3600));

        MockServerWebExchange exchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secured")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                        .build()
        );

        filter.filter(exchange, chain).block();

        assertTrue(invoked.get());
    }

    @Test
    void rateLimitFilterShouldRejectBlockedIpAndAllowOthers() {
        RateLimiterService rateLimiterService = new RateLimiterService(new RedisTemplate()) {
            @Override
            public boolean isAllowed(String ip) {
                return !"127.0.0.1".equals(ip);
            }
        };
        RateLimitFilter filter = new RateLimitFilter(rateLimiterService);
        AtomicBoolean invoked = new AtomicBoolean(false);
        GatewayFilterChain chain = exchange -> {
            invoked.set(true);
            return Mono.empty();
        };

        MockServerWebExchange blockedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secured").remoteAddress(new InetSocketAddress("127.0.0.1", 8080)).build()
        );

        filter.filter(blockedExchange, chain).block();
        assertEquals(HttpStatus.TOO_MANY_REQUESTS, blockedExchange.getResponse().getStatusCode());

        MockServerWebExchange allowedExchange = MockServerWebExchange.from(
                MockServerHttpRequest.get("/secured").remoteAddress(new InetSocketAddress("127.0.0.2", 8080)).build()
        );

        filter.filter(allowedExchange, chain).block();
        assertTrue(invoked.get());
        assertEquals(-10, filter.getOrder());
    }

    @Test
    void redisConfigShouldCreateReactiveTemplate() {
        ReactiveRedisConnectionFactory connectionFactory = mock(ReactiveRedisConnectionFactory.class);

        ReactiveStringRedisTemplate template = new RedisConfig().reactiveStringRedisTemplate(connectionFactory);

        assertNotNull(template);
    }

    @Test
    void corsConfigShouldCreateCorsWebFilter() {
        CorsWebFilter filter = new CorsConfig().corsWebFilter();

        assertNotNull(filter);
    }

    private JwtUtil createJwtUtil() {
        SecretKey key = Keys.secretKeyFor(io.jsonwebtoken.SignatureAlgorithm.HS256);
        String secret = Encoders.BASE64.encode(key.getEncoded());
        JwtUtil jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", secret);
        return jwtUtil;
    }

    private String createToken(JwtUtil jwtUtil, String username, String role, Integer userId, Instant expiration) {
        String secret = (String) ReflectionTestUtils.getField(jwtUtil, "secret");
        SecretKey key = Keys.hmacShaKeyFor(io.jsonwebtoken.io.Decoders.BASE64.decode(secret));
        return Jwts.builder()
                .subject(username)
                .claim("role", role)
                .claim("userId", userId)
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }
}
