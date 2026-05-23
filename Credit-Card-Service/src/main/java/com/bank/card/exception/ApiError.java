package com.bank.card.exception;

import java.time.LocalDateTime;

public class ApiError {

    private int status;
    private String message;
    private LocalDateTime timestamp = LocalDateTime.now();

    public ApiError(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() { return status; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}