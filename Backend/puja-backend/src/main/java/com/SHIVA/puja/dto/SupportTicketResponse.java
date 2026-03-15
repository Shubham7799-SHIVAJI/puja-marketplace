package com.SHIVA.puja.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SupportTicketResponse {

    private Long id;
    private String ticketCode;
    private String subject;
    private String priority;
    private String status;
    private String message;
    private String assignedTo;
    private LocalDateTime updatedAt;
}
