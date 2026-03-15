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
@Table(name = "customer_notifications")
@Data
public class CustomerNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "notification_type", nullable = false, length = 40)
    private String notificationType;

    @Column(name = "title", nullable = false, length = 160)
    private String title;

    @Column(name = "detail", nullable = false, length = 255)
    private String detail;

    @Column(name = "read_status", nullable = false)
    private Boolean readStatus;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}