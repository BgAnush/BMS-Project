package com.bank.card.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class CardRequestDTO {

    @NotBlank(message = "Card type is required")
    private String type;

}