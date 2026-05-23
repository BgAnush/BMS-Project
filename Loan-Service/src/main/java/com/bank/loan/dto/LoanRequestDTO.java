package com.bank.loan.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanRequestDTO {

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be greater than 0")
    @Max(value = 1200000, message = "Maximum loan amount is 12,00,000")
    private Double amount;

    @NotNull(message = "Tenure is required")
    @Min(value = 6, message = "Minimum tenure is 6 months")
    @Max(value = 60, message = "Maximum tenure is 60 months")
    private Integer tenureMonths;

    @NotBlank(message = "Loan type is required")
    @Pattern(regexp = "GOLD|HOME|PERSONAL", message = "Loan type must be GOLD, HOME, or PERSONAL")
    private String loanType;
}