package com.bank.card.dto;

import lombok.Data;

@Data
public class AccountResponseDTO {
    private String accountNumber;
    private Double balance;
}