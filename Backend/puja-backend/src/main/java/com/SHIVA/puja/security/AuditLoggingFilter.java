package com.SHIVA.puja.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Set;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.SHIVA.puja.entity.AuditLog;
import com.SHIVA.puja.repository.AuditLogRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class AuditLoggingFilter extends OncePerRequestFilter {

    private static final Set<String> MUTATING_METHODS = Set.of("POST", "PUT", "PATCH", "DELETE");

    private final AuditLogRepository auditLogRepository;

    public AuditLoggingFilter(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);

        if (!MUTATING_METHODS.contains(request.getMethod())) {
            return;
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AuditLog auditLog = new AuditLog();
        auditLog.setAction(request.getMethod());
        auditLog.setResourcePath(request.getRequestURI());
        auditLog.setStatusCode(response.getStatus());
        auditLog.setClientIp(request.getRemoteAddr());
        auditLog.setCreatedAt(LocalDateTime.now());
        auditLog.setDetails("Mutation request captured by audit filter.");
        if (authentication != null && authentication.isAuthenticated() && authentication.getName() != null) {
            auditLog.setActorEmail(authentication.getName());
            auditLog.setActorRole(authentication.getAuthorities().stream().findFirst().map(Object::toString).orElse("UNKNOWN"));
        }
        auditLogRepository.save(auditLog);
    }
}
