package com.SHIVA.puja.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.SHIVA.puja.config.AppProperties;
import com.SHIVA.puja.exception.ApiErrorResponse;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final AppProperties appProperties;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public RateLimitingFilter(AppProperties appProperties) {
        this.appProperties = appProperties;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String key = request.getRemoteAddr() + ":" + request.getRequestURI();
        int maxRequests = appProperties.getRateLimit().getRequests();
        long windowSeconds = appProperties.getRateLimit().getWindowSeconds();
        WindowCounter counter = counters.compute(key, (ignored, existing) -> {
            Instant now = Instant.now();
            if (existing == null || now.isAfter(existing.windowStart.plusSeconds(windowSeconds))) {
                return new WindowCounter(now, 1);
            }
            existing.count++;
            return existing;
        });

        if (counter.count > maxRequests) {
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

    private static class WindowCounter {
        private final Instant windowStart;
        private int count;

        private WindowCounter(Instant windowStart, int count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
