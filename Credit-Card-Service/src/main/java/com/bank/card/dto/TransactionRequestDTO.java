package com.bank.card.dto;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionRequestDTO {

    private String accountNumber;
    private String targetAccountNumber;
    private Double amount;
    private String type;
}