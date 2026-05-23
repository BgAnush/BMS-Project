package com.bank.notification.exception;

import com.bank.notification.dto.MailResponse;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<MailResponse> handleEmailException(
            EmailSendingException ex
    ) {

        log.error(
                "EmailSendingException occurred: {}",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new MailResponse(
                                ex.getMessage()
                        )
                );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<MailResponse> handleGeneralException(
            Exception ex
    ) {

        log.error(
                "Unexpected exception occurred",
                ex
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(
                        new MailResponse(
                                "Internal Server Error"
                        )
                );
    }
}