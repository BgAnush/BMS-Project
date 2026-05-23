package com.bank.transaction.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Pattern(regexp = "\\d{6}", message = "Account number must be exactly 6 digits")
    @Column(name = "account_number")
    private String accountNumber;

    @Pattern(regexp = "\\d{6}", message = "Account number must be exactly 6 digits")
    @Column(name = "target_account_number")   // ✅ FIXED
    private String targetAccountNumber;

    @PositiveOrZero(message = "Amount cannot be negative")
    private Double amount;

    private String type;

    @Column(name = "transaction_date")
    private LocalDateTime transactionDate;

    @PrePersist
    public void prePersist() {
        this.transactionDate = LocalDateTime.now();
    }
}