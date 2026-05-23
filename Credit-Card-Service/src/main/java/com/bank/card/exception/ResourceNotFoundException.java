package com.bank.card.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BankException {
    public ResourceNotFoundException(String msg) {
        super(msg, HttpStatus.NOT_FOUND);
    }
}