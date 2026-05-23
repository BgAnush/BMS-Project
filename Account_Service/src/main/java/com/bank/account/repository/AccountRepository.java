package com.bank.account.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.bank.account.model.Account;

import java.util.Optional;
import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByUserId(Integer userId);

    boolean existsByAccountNumber(String accountNumber);
}