package com.SHIVA.puja.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AdminIpRestrictionFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public AdminIpRestrictionFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uri = request.getRequestURI();
        if (!uri.startsWith("/api/admin")) {
            filterChain.doFilter(request, response);
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            filterChain.doFilter(request, response);
            return;
        }

        User user = userRepository.findTopByEmailOrderByIdDesc(authentication.getName()).orElse(null);
        if (user == null) {
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String role = user.getRole() == null ? "" : user.getRole().toUpperCase();
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            filterChain.doFilter(request, response);
            return;
        }

        String allowedIps = user.getAdminAllowedIps();
        if (allowedIps == null || allowedIps.isBlank()) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = resolveIp(request);
        boolean allowed = false;
        for (String candidate : allowedIps.split(",")) {
            if (clientIp.equals(candidate.trim())) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"code\":\"ADMIN_IP_RESTRICTED\",\"message\":\"Access denied from this IP\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String resolveIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        String realIp = request.getHeader("X-Real-IP");
        if (realIp != null && !realIp.isBlank()) {
            return realIp.trim();
        }

        return request.getRemoteAddr();
    }
}
