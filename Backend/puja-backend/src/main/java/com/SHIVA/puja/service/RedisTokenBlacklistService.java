package com.SHIVA.puja.service;

public interface RedisTokenBlacklistService {

    void blacklistTokenId(String tokenId, long ttlSeconds);

    boolean isTokenIdBlacklisted(String tokenId);

    void blacklistUser(String email, long ttlSeconds);

    boolean isUserBlacklisted(String email);
}
