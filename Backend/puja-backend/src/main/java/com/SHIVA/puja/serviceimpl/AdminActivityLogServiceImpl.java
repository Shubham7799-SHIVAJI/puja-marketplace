package com.SHIVA.puja.serviceimpl;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.SHIVA.puja.entity.AdminActivityLog;
import com.SHIVA.puja.entity.User;
import com.SHIVA.puja.exception.ApiException;
import com.SHIVA.puja.repository.AdminActivityLogRepository;
import com.SHIVA.puja.repository.UserRepository;
import com.SHIVA.puja.service.AdminActivityLogService;

import jakarta.servlet.http.HttpServletRequest;

@Service
@Transactional
public class AdminActivityLogServiceImpl implements AdminActivityLogService {

    private final AdminActivityLogRepository adminActivityLogRepository;
    private final UserRepository userRepository;

    public AdminActivityLogServiceImpl(AdminActivityLogRepository adminActivityLogRepository, UserRepository userRepository) {
        this.adminActivityLogRepository = adminActivityLogRepository;
        this.userRepository = userRepository;
    }

    @Override
    public void log(String actionType, String targetEntity, Long targetId, String details) {
        User admin = currentAdmin();

        AdminActivityLog log = new AdminActivityLog();
        log.setAdminId(admin.getId());
        log.setActionType(actionType);
        log.setTargetEntity(targetEntity);
        log.setTargetId(targetId == null ? 0L : targetId);
        log.setDetails(details);
        log.setIpAddress(resolveIpAddress());
        log.setCreatedAt(LocalDateTime.now());

        adminActivityLogRepository.save(log);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdminActivityLog> listRecent() {
        return adminActivityLogRepository.findTop100ByOrderByCreatedAtDesc();
    }

    private User currentAdmin() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getName())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "AUTH_REQUIRED", "Authentication is required.");
        }

        User admin = userRepository.findTopByEmailOrderByIdDesc(authentication.getName())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "USER_NOT_FOUND", "Authenticated user not found."));

        String role = admin.getRole() == null ? "" : admin.getRole().toUpperCase();
        if (!"ADMIN".equals(role) && !"SUPER_ADMIN".equals(role)) {
            throw new ApiException(HttpStatus.FORBIDDEN, "ADMIN_ONLY", "Admin privileges are required.");
        }

        return admin;
    }

    private String resolveIpAddress() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }

        HttpServletRequest request = attributes.getRequest();
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
