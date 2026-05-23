package com.bank.account.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponseDTO {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Account type is required")
    private String accType;

    @NotNull(message = "Balance is required")
    @PositiveOrZero(message = "Balance cannot be negative")
    private Double balance;

    @NotNull(message = "Interest rate is required")
    @PositiveOrZero(message = "Interest rate must be positive")
    private Double interestRate;

    @NotBlank(message = "Branch location is required")
    private String branchLocation;

    @NotNull(message = "Opening date is required")
    private LocalDateTime openingDate;

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Account status is required")
    private Boolean active;
}