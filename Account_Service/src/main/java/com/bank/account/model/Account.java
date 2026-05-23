package com.bank.account.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.*;
@Entity
@Table(name = "accounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", unique = true, nullable = false)
    private String accountNumber;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "acc_type")
    private String accType;

    private Double balance;

    private Double interestRate;

    private String branchLocation;

    @Column(name = "card_number", unique = true, nullable = false)
    private String cardNumber;

    private LocalDateTime openingDate;

    private Boolean active;

}