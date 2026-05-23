package com.bank.notification.service.impl;

import com.bank.notification.dto.RegistrationMailRequest;
import com.bank.notification.exception.EmailSendingException;
import com.bank.notification.service.EmailService;
import com.bank.notification.utility.MailTemplateBuilder;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Async
    @Override
    public void sendRegistrationMail(
            RegistrationMailRequest request
    ) {

        try {

            log.info(
                    "Sending registration mail to={}",
                    request.getEmail()
            );

            SimpleMailMessage message =
                    new SimpleMailMessage();

            message.setTo(request.getEmail());

            message.setSubject(
                    "Banking Management System - Registration Successful"
            );

            message.setText(
                    MailTemplateBuilder.buildRegistrationTemplate(
                            request.getFullName(),
                            request.getCustomerId(),
                            request.getRole()
                    )
            );

            mailSender.send(message);

            log.info(
                    "Registration mail sent successfully to={}",
                    request.getEmail()
            );

        } catch (Exception e) {

            log.error(
                    "Failed to send registration mail to={}",
                    request.getEmail(),
                    e
            );

            throw new EmailSendingException(
                    "Failed to send registration email"
            );
        }
    }
}