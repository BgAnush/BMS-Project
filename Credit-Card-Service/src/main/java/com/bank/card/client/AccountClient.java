package com.bank.card.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import com.bank.card.dto.AccountResponseDTO;

@FeignClient(name = "BANK-ACCOUNT-SERVICE")
public interface AccountClient {

    @GetMapping("/accounts/internal/user/{userId}")
    AccountResponseDTO getAccountByUserId(@PathVariable Long userId);

    @PutMapping("/accounts/internal/{accountNumber}/withdraw")
    void withdraw(@PathVariable String accountNumber,
                  @RequestParam Double amount);
}