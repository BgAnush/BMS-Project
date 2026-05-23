package com.bank.user.exception;

public class AgeValidationException extends RuntimeException {
    public AgeValidationException() {
        super("User must be at least 18 years old");
    }
}