package com.bank.card.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor

public class PinRequestDTO {

    @NotBlank
    @Pattern(regexp = "\\d{4}", message = "PIN must be 4 digits")
    private String pin;

    @NotBlank
    private String confirmPin;
}
