package com.bank.card.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class TransactionDTO {

    @NotBlank
    private String cardNumber;

    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "PIN must be 4 digits")
    private String pin;

    @NotNull
    @Positive
    private Double amount;

    @NotBlank
    private String merchant;

    @NotBlank
    private String type;
}