package com.bank.notification.controller;

import com.bank.notification.dto.MailResponse;
import com.bank.notification.dto.RegistrationMailRequest;
import com.bank.notification.service.EmailService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final EmailService emailService;

    @PostMapping("/email/register")
    public ResponseEntity<MailResponse> sendRegistrationMail(
            @Valid
            @RequestBody
            RegistrationMailRequest request
    ) {

        log.info(
                "Registration mail API called for email={}",
                request.getEmail()
        );

        emailService.sendRegistrationMail(request);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(
                        new MailResponse(
                                "Registration mail sent successfully"
                        )
                );
    }
}