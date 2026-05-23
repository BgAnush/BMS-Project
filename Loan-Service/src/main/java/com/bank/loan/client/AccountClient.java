package com.bank.loan.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.bank.loan.dto.AccountResponseDTO;

@FeignClient(name = "BANK-ACCOUNT-SERVICE")
public interface AccountClient {

    @GetMapping("/accounts/internal/user/{userId}")
    AccountResponseDTO getAccountByUserId(@PathVariable Long userId);

    @PutMapping("/accounts/internal/{accountNumber}/deposit")
    void deposit(@PathVariable String accountNumber,
                 @RequestParam Double amount);

    @PutMapping("/accounts/internal/{accountNumber}/withdraw")
    void debit(@PathVariable String accountNumber,
               @RequestParam Double amount);
}