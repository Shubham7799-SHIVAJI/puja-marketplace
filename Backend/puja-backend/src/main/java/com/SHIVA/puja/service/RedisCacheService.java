package com.SHIVA.puja.service;

import java.time.Duration;
import java.util.Optional;

public interface RedisCacheService {

    void put(String namespace, String key, String value, Duration ttl);

    Optional<String> get(String namespace, String key);

    void evict(String namespace, String key);
}
