package com.transport.auth_service.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(AuthException.class)
    public ResponseEntity<ErrorResponse> handleAuthException(AuthException ex) {
        org.springframework.http.HttpStatus status = ex.getStatus();
        if (status == null) {
            status = org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
        }
        ErrorResponse errorResponse = new ErrorResponse(status.value(), ex.getMessage());
        return new ResponseEntity<>(errorResponse, status);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        logger.error("Validation exception handled: {}", ex.getMessage());
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error ->
                errors.put(error.getField(), error.getDefaultMessage()));
        ErrorResponse errorResponse = new ErrorResponse(HttpStatus.BAD_REQUEST.value(), "Validation failed", errors);
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex) {
        logger.error("Global exception handled: {}", ex.getMessage());
        ErrorResponse errorResponse = new ErrorResponse(500, "An unexpected error occurred: " + ex.getMessage());
        return new ResponseEntity<>(errorResponse, org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR);
    }

}
