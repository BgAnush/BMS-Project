package com.bank.loan.exception;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.MethodArgumentNotValidException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ResourceNotFoundException ex) {
        return new ResponseEntity<>(
                new ApiError(404, ex.getMessage()),
                HttpStatus.NOT_FOUND
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBad(BadRequestException ex) {
        return new ResponseEntity<>(
                new ApiError(400, ex.getMessage()),
                HttpStatus.BAD_REQUEST
        );
    }

    // ✅ VALIDATION HANDLER
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {

        String msg = ex.getBindingResult()
                .getFieldErrors()
                .get(0)
                .getDefaultMessage();

        return new ResponseEntity<>(
                new ApiError(400, msg),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGeneric(Exception ex) {
        return new ResponseEntity<>(
                new ApiError(500, ex.getMessage()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}