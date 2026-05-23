package com.bank.loan.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor

public class AccountResponseDTO {

    private String accountNumber;
    private Double balance;

}