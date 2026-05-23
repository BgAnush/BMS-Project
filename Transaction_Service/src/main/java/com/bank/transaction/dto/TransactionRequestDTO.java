package com.bank.transaction.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionRequestDTO {

    @NotBlank(message = "Target account is required")
    @Pattern(regexp = "\\d{6}", message = "Target account must be exactly 6 digits")
    private String targetAccountNumber;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    private Double amount;
}