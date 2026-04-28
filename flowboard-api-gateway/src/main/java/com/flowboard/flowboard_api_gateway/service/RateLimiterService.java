package com.flowboard.flowboard_api_gateway.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
public class RateLimiterService {
    private static final Logger log = LoggerFactory.getLogger(RateLimiterService.class);

    private final RedisTemplate redisTemplate;

    public RateLimiterService(RedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Value("${app.rate-limit.enabled:false}")
    private boolean enabled;

    private static final int LIMIT = 100;
    private static final Duration WINDOW = Duration.ofSeconds(60);

    public boolean isAllowed(String ip) {
        if (!enabled) {
            return true;
        }

        String key = "rate:ip:" + ip;

        try {
            Long currentCount = redisTemplate.opsForValue().increment(key);

            if (currentCount == null) {
                log.warn("Redis rate-limit counter returned null for {}", ip);
                return true;
            }

            if (currentCount.equals(1L)) {
                redisTemplate.expire(key, WINDOW);
                return true;
            }

            return currentCount <= LIMIT;
        } catch (Exception ex) {
            log.warn("Rate limiter is unavailable. Allowing request for {}. Cause: {}", ip, ex.getMessage());
            return true;
        }
    }
}
