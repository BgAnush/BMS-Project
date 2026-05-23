package com.bank.auth.client;

import com.bank.auth.dto.RegistrationMailRequest;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "NOTIFICATION-SERVICE")
public interface NotificationFeignClient {

    @PostMapping("/notification/email/register")
    void sendRegistrationMail(
            @RequestBody
            RegistrationMailRequest request
    );
}