package com.bank.loan.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import lombok.*;

@Entity
@Table(name = "loans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Loan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long loanId;

    @Pattern(regexp = "[A-Z]{2}-\\d{8}", message = "Invalid loan number format")
    private String loanNumber;

    @NotNull
    private Long userId;

    @NotBlank
    @Pattern(regexp = "\\d{6}", message = "Account number must be 6 digits")
    private String accountNumber;

    @NotNull
    @Positive
    @Max(value = 1200000, message = "Max loan amount is 12,00,000")
    private Double loanAmount;

    @NotNull
    @Positive
    private Double interestRate;

    @NotNull
    @Min(value = 6, message = "Minimum tenure is 6 months")
    @Max(value = 60, message = "Maximum tenure is 60 months")
    private Integer tenureMonths;

    @Positive
    private Double emiAmount;

    @NotBlank
    @Pattern(regexp = "GOLD|HOME|PERSONAL", message = "Invalid loan type")
    private String loanType;

    @NotBlank
    @Pattern(regexp = "PENDING|ACTIVE|REJECTED|CLOSED", message = "Invalid status")
    private String status;

    private LocalDateTime createdAt;
}