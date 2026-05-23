package com.bank.auth.dto;

import lombok.Data;

@Data
public class RegistrationMailRequest {

    private String email;

    private String fullName;

    private String customerId;

    private String role;
}