package com.bank.transaction.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private Map<String, Object> buildResponse(String message, HttpStatus status) {
        return Map.of(
                "timestamp", LocalDateTime.now(),
                "status", status.value(),
                "error", message
        );
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        log.error("Unauthorized: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED));
    }

    @ExceptionHandler(InvalidHeaderException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidHeader(InvalidHeaderException ex) {
        log.error("Invalid Header: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, Object>> handleBalance(InsufficientBalanceException ex) {
        log.error("Balance Error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        log.error("Validation Error: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGeneric(Exception ex) {
        log.error("Unexpected Error: ", ex);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(buildResponse("Internal Server Error", HttpStatus.INTERNAL_SERVER_ERROR));
    }
}