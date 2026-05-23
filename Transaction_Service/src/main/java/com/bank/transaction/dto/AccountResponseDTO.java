package com.bank.transaction.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDTO {

    @Pattern(regexp = "\\d{6}", message = "Account number must be exactly 6 digits")
    private String accountNumber;

    private Integer userId;

    @PositiveOrZero(message = "Balance cannot be negative")
    private Double balance;

    private Boolean active;
}