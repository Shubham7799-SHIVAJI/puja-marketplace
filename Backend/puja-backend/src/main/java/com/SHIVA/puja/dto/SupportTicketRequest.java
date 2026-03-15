package com.SHIVA.puja.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SupportTicketRequest {

    @NotBlank(message = "Ticket code is required.")
    private String ticketCode;

    @NotBlank(message = "Subject is required.")
    private String subject;

    @NotBlank(message = "Priority is required.")
    private String priority;

    @NotBlank(message = "Status is required.")
    private String status;

    private String message;

    private String assignedTo;
}
