package com.bank.account.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDTO {

    @NotNull(message = "Profile ID cannot be null")
    private Integer profileId;

    @NotBlank(message = "Customer ID is required")
    private String customerId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email ID is required")
    private String emailId;

    @NotBlank(message = "Account number is required")
    private String accountNumber;
}