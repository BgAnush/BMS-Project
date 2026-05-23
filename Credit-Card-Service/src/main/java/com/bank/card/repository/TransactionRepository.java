package com.bank.card.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.card.model.CardTransaction;

public interface TransactionRepository extends JpaRepository<CardTransaction, Long> {
}
