package com.bank.auth.exception;

public class AuthenticationFailedException extends BaseException {
    public AuthenticationFailedException(String message) {
        super(message);
    }
}