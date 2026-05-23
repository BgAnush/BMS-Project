package com.bank.notification.service;

import com.bank.notification.dto.RegistrationMailRequest;

public interface EmailService {

    void sendRegistrationMail(
            RegistrationMailRequest request
    );
}