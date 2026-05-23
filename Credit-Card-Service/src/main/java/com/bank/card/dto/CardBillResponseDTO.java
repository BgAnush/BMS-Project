package com.bank.card.dto;

import java.time.LocalDate;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardBillResponseDTO {

	private Double totalDue;
    private Double minimumDue;    
    private Double paidAmount;
    private Double remainingDue;
    private Double penalty;        
    private Double interest;       
    private String message;
    private LocalDate dueDate;
}