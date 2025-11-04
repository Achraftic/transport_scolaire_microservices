package com.transport.auth_service.exception;

import org.springframework.http.HttpStatus;

public class AuthException extends RuntimeException {

    private final HttpStatus status;

    public AuthException(String message, HttpStatus status) {
        super(message);
        if (status == null) {
            throw new IllegalArgumentException("HttpStatus cannot be null");
        }
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
