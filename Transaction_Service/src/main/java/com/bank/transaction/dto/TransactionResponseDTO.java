package com.bank.transaction.dto;

import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TransactionResponseDTO {

    private Integer id;
    private String accountNumber;
    private String targetAccountNumber;
    private Double amount;
    private String type;
    private LocalDateTime transactionDate;
}