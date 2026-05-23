package com.bank.transaction.dto;

import lombok.*;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionInternalDTO {

    @NotBlank(message = "Account number is required")
    private String accountNumber;

    private String targetAccountNumber; // nullable for deposit/withdraw

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Transaction type is required")
    private String type; // DEPOSIT / WITHDRAW / TRANSFER
}