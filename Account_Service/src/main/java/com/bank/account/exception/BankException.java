package com.bank.account.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class BankException extends RuntimeException {

    private final HttpStatus status;

    public BankException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}