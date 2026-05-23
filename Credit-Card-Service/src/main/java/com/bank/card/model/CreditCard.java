package com.bank.card.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;

import java.time.*;
import lombok.*;
import com.bank.card.enums.CardStatus;
import com.bank.card.enums.CardType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String cardNumber;
    private String pin;
    private Long userId;
    @Min(0)
    private Double creditLimit;
    private Double availableLimit;
    private Double usedLimit;

    @Enumerated(EnumType.STRING)
    private CardType type;

    private Double interestRate;

    @Enumerated(EnumType.STRING)
    private CardStatus status;


    private LocalDateTime createdAt;
    private LocalDate expiryDate;
	

}