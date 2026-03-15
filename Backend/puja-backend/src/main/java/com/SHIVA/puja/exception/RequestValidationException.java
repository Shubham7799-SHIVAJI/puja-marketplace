package com.SHIVA.puja.exception;

import java.util.Collections;
import java.util.Map;

import lombok.Getter;

@Getter
public class RequestValidationException extends RuntimeException {

    private final Map<String, String> fieldErrors;

    public RequestValidationException(String message, Map<String, String> fieldErrors) {
        super(message);
        this.fieldErrors = Collections.unmodifiableMap(fieldErrors);
    }
}