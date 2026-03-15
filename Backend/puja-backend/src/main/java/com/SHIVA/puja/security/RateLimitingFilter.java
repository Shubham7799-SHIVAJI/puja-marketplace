package com.SHIVA.puja.security;

import java.io.IOException;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.SHIVA.puja.config.AppProperties;
import com.SHIVA.puja.exception.ApiErrorResponse;
import com.SHIVA.puja.service.RedisRateLimiterService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final RedisRateLimiterService redisRateLimiterService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public RateLimitingFilter(AppProperties appProperties, RedisRateLimiterService redisRateLimiterService) {
        this.appProperties = appProperties;
        this.redisRateLimiterService = redisRateLimiterService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        int maxRequests = appProperties.getRateLimit().getRequests();
        long windowSeconds = appProperties.getRateLimit().getWindowSeconds();
        boolean allowed = redisRateLimiterService.allowRequest(key, maxRequests, (int) windowSeconds);

        if (!allowed) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            objectMapper.writeValue(response.getWriter(), ApiErrorResponse.builder()
                    .status(HttpStatus.TOO_MANY_REQUESTS.value())
                    .code("RATE_LIMIT_EXCEEDED")
                    .error(HttpStatus.TOO_MANY_REQUESTS.getReasonPhrase())
                    .message("Too many requests. Please slow down and try again.")
                    .fieldErrors(Map.of())
                    .timestamp(java.time.LocalDateTime.now())
                    .build());
            return;
        }

        filterChain.doFilter(request, response);
    }
}
