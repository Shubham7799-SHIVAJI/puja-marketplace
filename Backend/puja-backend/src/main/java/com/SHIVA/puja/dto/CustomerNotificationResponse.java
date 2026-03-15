package com.SHIVA.puja.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomerNotificationResponse {

    private Long id;
    private String type;
    private String title;
    private String detail;
    private Boolean read;
    private LocalDateTime createdAt;
}