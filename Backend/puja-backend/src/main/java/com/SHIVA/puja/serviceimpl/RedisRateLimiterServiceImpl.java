package com.SHIVA.puja.serviceimpl;

import java.time.Duration;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.SHIVA.puja.service.RedisRateLimiterService;

@Service
public class RedisRateLimiterServiceImpl implements RedisRateLimiterService {

    private static final String PREFIX = "daily-puja:ratelimit";
    private final StringRedisTemplate redisTemplate;

    public RedisRateLimiterServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean allowRequest(String clientKey, int maxRequests, int windowSeconds) {
        String key = PREFIX + ':' + sanitize(clientKey);
        try {
            Long value = redisTemplate.opsForValue().increment(key);
            if (value != null && value == 1L) {
                redisTemplate.expire(key, Duration.ofSeconds(windowSeconds));
            }
            return value == null || value <= maxRequests;
        } catch (DataAccessException ignored) {
            // Fail-open to protect availability if Redis is down.
            return true;
        }
    }

    private String sanitize(String value) {
        return value == null ? "na" : value.trim().replaceAll("[^A-Za-z0-9:_-]", "_");
    }
}
