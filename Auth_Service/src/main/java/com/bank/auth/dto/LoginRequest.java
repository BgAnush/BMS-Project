package com.bank.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    // =========================================================
    // CUSTOMER ID VALIDATION
    // FORMAT: ADMXXXXXX / MNGXXXXXX / CUSXXXXXX
    // =========================================================
    @NotBlank(message = "Customer ID is required")
    @Pattern(
        regexp = "^(ADM|MNG|CUS)[A-Z0-9]{6}$",
        message = "Customer ID must be in format ADMXXXXXX / MNGXXXXXX / CUSXXXXXX"
    )
    private String customerId;

    // =========================================================
    // PASSWORD VALIDATION
    // Minimum 8 characters:
    // - At least 1 uppercase
    // - At least 1 lowercase
    // - At least 1 digit
    // - At least 1 special character
    // =========================================================
    @NotBlank(message = "Password is required")
    @Pattern(
        regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
        message = "Password must contain at least 8 characters, including uppercase, lowercase, number, and special character"
    )
    private String password;
}