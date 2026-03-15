package com.SHIVA.puja.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "admin_activity_logs")
@Data
public class AdminActivityLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "admin_id", nullable = false)
    private Long adminId;

    @Column(name = "action_type", nullable = false, length = 80)
    private String actionType;

    @Column(name = "target_entity", nullable = false, length = 80)
    private String targetEntity;

    @Column(name = "target_id", nullable = false)
    private Long targetId;

    @Column(name = "ip_address", length = 64)
    private String ipAddress;

    @Column(name = "details", length = 255)
    private String details;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
