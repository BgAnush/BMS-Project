package com.bank.user.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.extern.slf4j.Slf4j;


import java.time.LocalDate;

@Entity
@Table(name = "bank_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Slf4j
public class BankUser {

    @Id
    private Integer profileId;

    @Column(unique = true, nullable = false)
    private String customerId;
    @Column(nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String emailId;
    @Column(unique = true, nullable = false, length = 10)
    private String mobileNumber;

    private String gender;
    @Column(length = 500)
    private String address;

    @Column(columnDefinition = "LONGTEXT")
    private String about;

    private LocalDate dateOfBirth;

    @Column(unique = true, nullable = false)
    private Long accountNumber;

    @Column(columnDefinition = "LONGTEXT")
    private String image;
}