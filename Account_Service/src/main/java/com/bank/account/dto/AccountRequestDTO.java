package com.bank.account.dto;

import jakarta.validation.constraints.*;
import lombok.*;
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AccountRequestDTO {

    @NotNull(message = "User ID is required")
    private Integer userId;

    @NotBlank(message = "Account number is required")
    @Size(min = 10, max = 20, message = "Account number must be valid")
    private String accountNumber;

    @NotBlank(message = "Account type is required")
    private String accType;

    @PositiveOrZero(message = "Balance cannot be negative")
    private Double balance;

    private String branchLocation;

}