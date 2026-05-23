package com.bank.notification.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class RegistrationMailRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String fullName;

    @NotBlank
    private String customerId;

    @NotBlank
    private String role;
}