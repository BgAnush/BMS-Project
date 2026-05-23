package com.bank.user.exception;

import org.springframework.web.bind.annotation.*;
import org.springframework.http.*;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String KEY_ERROR = "error";
    private static final String KEY_STATUS = "status";

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handle(RuntimeException ex) {
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                        KEY_ERROR, ex.getMessage(),
                        KEY_STATUS, HttpStatus.BAD_REQUEST.value()
                ));
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorized(UnauthorizedException ex) {
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Map.of(
                        KEY_ERROR, ex.getMessage(),
                        KEY_STATUS, HttpStatus.UNAUTHORIZED.value()
                ));
    }

    @ExceptionHandler(AccountGenerationException.class)
    public ResponseEntity<Map<String, Object>> handleAccountGen(AccountGenerationException ex) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of(
                        KEY_ERROR, ex.getMessage(),
                        KEY_STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value()
                ));
    }
}