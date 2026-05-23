package com.bank.card.exception;

import org.springframework.http.HttpStatus;

public class UnauthorizedException extends BankException {
    public UnauthorizedException(String msg) {
        super(msg, HttpStatus.UNAUTHORIZED);
    }
}