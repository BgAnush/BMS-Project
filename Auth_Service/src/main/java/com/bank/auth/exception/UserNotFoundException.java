package com.bank.auth.exception;

public class UserNotFoundException extends BaseException {
    public UserNotFoundException(String msg) {
        super(msg);
    }
}