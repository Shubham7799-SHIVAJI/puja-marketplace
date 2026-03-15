package com.SHIVA.puja.controllers;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.SHIVA.puja.dto.AdminWorkspaceResponse;
import com.SHIVA.puja.service.AdminWorkspaceService;

@RestController
@RequestMapping({"/admin-dashboard", "/api/v1/admin-dashboard", "/api/admin/dashboard"})
@CrossOrigin(origins = "http://localhost:4200")
@PreAuthorize("hasAnyRole('ADMIN','SUPER_ADMIN')")
public class AdminWorkspaceController {

    private final AdminWorkspaceService adminWorkspaceService;

    public AdminWorkspaceController(AdminWorkspaceService adminWorkspaceService) {
        this.adminWorkspaceService = adminWorkspaceService;
    }

    @GetMapping("/workspace")
    public AdminWorkspaceResponse getWorkspace() {
        return adminWorkspaceService.getWorkspace();
    }
}
