package com.bank.card.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.bank.card.enums.TransactionStatus;
import com.bank.card.enums.TransactionType;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "card_transactions")
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long cardId;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private String merchant;

    private LocalDateTime transactionTime;

    @Enumerated(EnumType.STRING)
    private TransactionStatus status;

    private String message;

}