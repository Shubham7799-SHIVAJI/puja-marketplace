package com.SHIVA.puja.service;

public interface RedisRateLimiterService {

    boolean allowRequest(String clientKey, int maxRequests, int windowSeconds);
}
