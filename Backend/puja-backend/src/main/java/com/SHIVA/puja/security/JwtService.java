package com.SHIVA.puja.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.SHIVA.puja.config.AppProperties;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Service
public class JwtService {

    private final AppProperties appProperties;

    public JwtService(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    public String generateToken(UserDetails userDetails, Map<String, Object> claims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(claims)
            .id(UUID.randomUUID().toString())
                .subject(userDetails.getUsername())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(appProperties.getSecurity().getJwtExpirationMinutes(), ChronoUnit.MINUTES)))
                .signWith(signingKey())
                .compact();
    }

    public String generateScopedToken(String subject, String scope, long expirationMinutes, Map<String, Object> claims) {
        Instant now = Instant.now();
        Map<String, Object> tokenClaims = new java.util.HashMap<>(claims == null ? Map.of() : claims);
        tokenClaims.put("scope", scope);

        return Jwts.builder()
                .claims(tokenClaims)
                .subject(subject)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expirationMinutes, ChronoUnit.MINUTES)))
                .signWith(signingKey())
                .compact();
    }

    public boolean isScopedTokenValid(String token, String expectedScope, String expectedSubject) {
        try {
            Claims claims = parseClaims(token);
            String subject = claims.getSubject();
            String scope = claims.get("scope", String.class);
            boolean notExpired = claims.getExpiration() != null && claims.getExpiration().after(new Date());

            return notExpired
                    && expectedSubject != null
                    && expectedSubject.equals(subject)
                    && expectedScope != null
                    && expectedScope.equals(scope);
        } catch (JwtException | IllegalArgumentException exception) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String extractTokenId(String token) {
        return extractClaim(token, Claims::getId);
    }

    public <T> T extractClaim(String token, Function<Claims, T> resolver) {
        Claims claims = parseClaims(token);
        return resolver.apply(claims);
    }

    public long remainingTtlSeconds(String token) {
        try {
            Claims claims = parseClaims(token);
            long seconds = (claims.getExpiration().toInstant().toEpochMilli() - System.currentTimeMillis()) / 1000;
            return Math.max(0, seconds);
        } catch (RuntimeException exception) {
            return 0;
        }
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        String username = extractUsername(token);
        return username != null && username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser().verifyWith((javax.crypto.SecretKey) signingKey()).build().parseSignedClaims(token).getPayload();
    }

    private Key signingKey() {
        String secret = appProperties.getSecurity().getJwtSecret();
        byte[] keyBytes;
        if (secret.matches("^[A-Za-z0-9+/=]+$") && secret.length() >= 44) {
            keyBytes = Decoders.BASE64.decode(secret);
        } else {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes.length >= 32 ? keyBytes : String.format("%-32s", secret).getBytes(StandardCharsets.UTF_8));
    }
}
