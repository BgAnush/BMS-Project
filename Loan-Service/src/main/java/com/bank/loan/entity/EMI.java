package com.bank.loan.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "emis")
public class EMI {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long emiId;

    @NotNull
    private Long loanId;

    @NotNull
    @Min(1)
    private Integer emiNumber;

    @NotNull
    private LocalDate dueDate;

    @NotNull
    @Positive
    private Double amount;

    @NotBlank
    @Pattern(regexp = "PENDING|PAID|CLOSED", message = "Invalid EMI status")
    private String status;

    private LocalDate paidDate;
}