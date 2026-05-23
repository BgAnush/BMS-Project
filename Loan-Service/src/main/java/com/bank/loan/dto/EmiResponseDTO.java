package com.bank.loan.dto;

import java.time.LocalDate;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class EmiResponseDTO {

    private Integer emiNumber;
    private LocalDate dueDate;
    private Double amount;
    private String status;
	

}