package com.bank.transaction.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.transaction.model.Transaction;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Integer> {

    List<Transaction> findByAccountNumberOrTargetAccountNumber(String accountNumber,String targetAccountNumber);
}