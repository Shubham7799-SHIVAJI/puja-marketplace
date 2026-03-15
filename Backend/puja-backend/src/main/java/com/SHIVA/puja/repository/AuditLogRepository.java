package com.SHIVA.puja.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.SHIVA.puja.entity.AuditLog;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
