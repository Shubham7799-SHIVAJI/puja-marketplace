package com.SHIVA.puja.controllers;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SHIVA.puja.dto.AdminWorkspaceResponse;
import com.SHIVA.puja.entity.AdminActivityLog;
import com.SHIVA.puja.service.AdminActivityLogService;
import com.SHIVA.puja.service.AdminWorkspaceService;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminGatewayController {

    private final AdminWorkspaceService adminWorkspaceService;
    private final AdminActivityLogService adminActivityLogService;

    public AdminGatewayController(AdminWorkspaceService adminWorkspaceService, AdminActivityLogService adminActivityLogService) {
        this.adminWorkspaceService = adminWorkspaceService;
        this.adminActivityLogService = adminActivityLogService;
    }

    @GetMapping("/dashboard/workspace")
    public AdminWorkspaceResponse getWorkspace() {
        return adminWorkspaceService.getWorkspace();
    }

    @GetMapping("/audit-logs")
    public List<AdminActivityLog> listAuditLogs() {
        return adminActivityLogService.listRecent();
    }
}
