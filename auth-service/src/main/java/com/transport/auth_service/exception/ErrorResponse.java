package com.transport.auth_service.exception;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {
    private int statusCode;
    private String message;
    private LocalDateTime timestamp;
    private Map<String, String> errors;

    public ErrorResponse(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public ErrorResponse(int statusCode, String message, Map<String, String> errors) {
        this.statusCode = statusCode;
        this.message = message;
        this.timestamp = LocalDateTime.now();
        this.errors = errors;
    }

    // Getters and setters
    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
