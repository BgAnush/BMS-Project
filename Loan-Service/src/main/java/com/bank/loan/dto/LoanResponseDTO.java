package com.bank.loan.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoanResponseDTO {

    private Long loanId;
    private Double emiAmount;
    private String status;
	
}