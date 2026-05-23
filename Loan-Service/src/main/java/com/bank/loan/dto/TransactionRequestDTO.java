package com.bank.loan.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestDTO {

    @NotBlank(message = "Account number is required")
    @Pattern(regexp = "\\d{6}", message = "Account number must be 6 digits")
    private String accountNumber;

    @Pattern(regexp = "\\d{6}", message = "Target account must be 6 digits")
    private String targetAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;

    @NotBlank(message = "Transaction type is required")
    @Pattern(regexp = "LOAN_CREDIT",
            message = "Invalid transaction type")
    private String type;
}