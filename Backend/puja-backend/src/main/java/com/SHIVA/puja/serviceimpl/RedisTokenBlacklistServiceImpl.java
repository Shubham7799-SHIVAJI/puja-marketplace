package com.SHIVA.puja.serviceimpl;

import java.time.Duration;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import com.SHIVA.puja.service.RedisTokenBlacklistService;

@Service
public class RedisTokenBlacklistServiceImpl implements RedisTokenBlacklistService {

    private static final String TOKEN_PREFIX = "daily-puja:token:blacklist:jti";
    private static final String USER_PREFIX = "daily-puja:token:blacklist:user";
    private final StringRedisTemplate redisTemplate;

    public RedisTokenBlacklistServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void blacklistTokenId(String tokenId, long ttlSeconds) {
        if (tokenId == null || tokenId.isBlank() || ttlSeconds <= 0) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(tokenKey(tokenId), "1", Duration.ofSeconds(ttlSeconds));
        } catch (DataAccessException ignored) {
            // Best-effort blacklist.
        }
    }

    @Override
    public boolean isTokenIdBlacklisted(String tokenId) {
        if (tokenId == null || tokenId.isBlank()) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(tokenKey(tokenId)));
        } catch (DataAccessException ignored) {
            return false;
        }
    }

    @Override
    public void blacklistUser(String email, long ttlSeconds) {
        if (email == null || email.isBlank() || ttlSeconds <= 0) {
            return;
        }

        try {
            redisTemplate.opsForValue().set(userKey(email), "1", Duration.ofSeconds(ttlSeconds));
        } catch (DataAccessException ignored) {
            // Best-effort blacklist.
        }
    }

    @Override
    public boolean isUserBlacklisted(String email) {
        if (email == null || email.isBlank()) {
            return false;
        }

        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(userKey(email)));
        } catch (DataAccessException ignored) {
            return false;
        }
    }

    private String tokenKey(String tokenId) {
        return TOKEN_PREFIX + ':' + tokenId.trim();
    }

    private String userKey(String email) {
        return USER_PREFIX + ':' + email.trim().toLowerCase();
    }
}
