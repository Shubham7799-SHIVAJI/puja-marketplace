package com.SHIVA.puja.serviceimpl;

import java.time.Duration;
import java.util.Optional;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.SHIVA.puja.service.RedisCacheService;

@Service
public class RedisCacheServiceImpl implements RedisCacheService {

    private static final String PREFIX = "daily-puja";
    private final StringRedisTemplate redisTemplate;

    public RedisCacheServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void put(String namespace, String key, String value, Duration ttl) {
        String namespacedKey = namespaced(namespace, key);
        try {
            if (ttl == null || ttl.isZero() || ttl.isNegative()) {
                redisTemplate.opsForValue().set(namespacedKey, value);
            } else {
                redisTemplate.opsForValue().set(namespacedKey, value, ttl);
            }
        } catch (DataAccessException ignored) {
            // Redis outages should not break request flow.
        }
    }

    @Override
    public Optional<String> get(String namespace, String key) {
        try {
            return Optional.ofNullable(redisTemplate.opsForValue().get(namespaced(namespace, key)));
        } catch (DataAccessException ignored) {
            return Optional.empty();
        }
    }

    @Override
    public void evict(String namespace, String key) {
        try {
            redisTemplate.delete(namespaced(namespace, key));
        } catch (DataAccessException ignored) {
            // Ignore cache eviction failures.
        }
    }

    private String namespaced(String namespace, String key) {
        return PREFIX + ':' + sanitize(namespace) + ':' + sanitize(key);
    }

    private String sanitize(String value) {
        return value == null ? "na" : value.trim().replaceAll("[^A-Za-z0-9:_-]", "_");
    }
}
