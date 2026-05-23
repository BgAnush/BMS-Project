package com.bank.notification.exception;

public class EmailSendingException
        extends RuntimeException {

    public EmailSendingException(String message) {

        super(message);
    }
}