package com.bank.card.exception;

import org.springframework.http.HttpStatus;

public class BadRequestException extends BankException {
    public BadRequestException(String msg) {
        super(msg, HttpStatus.BAD_REQUEST);
    }
}