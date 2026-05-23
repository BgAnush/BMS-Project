package com.bank.transaction.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;
import com.bank.transaction.dto.AccountResponseDTO;

@FeignClient(name = "BANK-ACCOUNT-SERVICE")
public interface AccountClient {

    @GetMapping("/accounts/internal/user/{userId}")
    AccountResponseDTO getAccountByUserId(@PathVariable("userId") Integer userId);

    @GetMapping("/accounts/internal/{accountNumber}")
    AccountResponseDTO getInternal(@PathVariable("accountNumber") String accountNumber);

    @PutMapping("/accounts/internal/{accountNumber}/deposit")
    String deposit(@PathVariable("accountNumber") String accountNumber, @RequestParam("amount") Double amount);

    @PutMapping("/accounts/internal/{accountNumber}/withdraw")
    String withdraw(@PathVariable("accountNumber") String accountNumber, @RequestParam("amount") Double amount);

}