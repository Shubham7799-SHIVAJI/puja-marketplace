package com.SHIVA.puja.exception;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ApiErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String code;
    private String error;
    private String message;
    private Map<String, String> fieldErrors;

}